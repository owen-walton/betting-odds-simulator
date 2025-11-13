/**
 * @author Owen Walton
 * This class is used to coordinate all the ingestion logic
 * so that the controller only needs to access this and DatabaseInitialiser.java
 * ,
 * this class is designed to keep different parts of the program
 * (e.g/ DB interaction, API requests, Sheet parsing) separate so that the design is modular and clean
 * and allowing clear abstraction when debugging and making further edits.
 */

package com.betwise.oddscalc.service;

import com.betwise.oddscalc.database.dao.*;
import com.betwise.oddscalc.entity.*;
import com.betwise.oddscalc.ingestdata.CricAPIClient;
import com.betwise.oddscalc.ingestdata.CricSheetParser;
import com.betwise.oddscalc.ingestdata.ingestutils.HTTPClient;
import com.betwise.oddscalc.ingestdata.ingestutils.TeamAliasMap;
import com.betwise.oddscalc.ingestdata.ingestutils.VenueDeduplicator;

import java.io.IOException;
import java.util.*;

public class IngestionService {
    // heavy logic behind team home venue population is handled in DAO layer but this wrapper provides an access point
    public void populateTeamHomeVenue() {
        System.out.println("Populating Team Home Venue");
        try (TeamHomeVenueDAO teamHomeVenueDAO = new TeamHomeVenueDAO()) {
            teamHomeVenueDAO.populateTeamHomeVenue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Team Home Venue populated");
        }
    }

    // ingest all countries from cricAPIs country list into Team table
    // important because the teams are the method of identifying if a cricAPI match is international
    // this is the only function that is strenuous on API hits but is only ran on launch (may need to switch key)
    public void ingestCountriesFromCricAPI() {
        System.out.println("Ingesting countries from cricAPI");
        TeamAliasMap teamAliasMap = new TeamAliasMap();
        CricAPIClient cricAPIClient = new CricAPIClient(new HTTPClient(), null);
        try (TeamDAO teamDAO = new TeamDAO()) {
            List<Team> teams = new ArrayList<>();
            for (String country : cricAPIClient.loadInternationalCountries()) {
                teams.add(new Team(0, teamAliasMap.aliasCheck(country)));
            }
            teamDAO.bulkInsertIfNotExists(teams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Countries ingested");
        }
    }

    // Inserts any matches from last 7 days that aren't already in DB into the DB
    public void updateLast7Days() throws IOException {
        System.out.println("Updating last 7 days");
        try (
                TeamDAO teamDAO = new TeamDAO();
                CricketMatchDAO cricketMatchDAO = new CricketMatchDAO()
        ){
            System.out.println("Initialising client");
            // give client team names from DB so it can only do international matches without having to talk to DB layer
            CricAPIClient cricAPIClient = new CricAPIClient(new HTTPClient(), teamDAO.getAllTeamNames());

            System.out.println("Beginning parse");
            // create a temporary schema object to store the matches parsed
            CricketMatchDataSchema schema = cricAPIClient.parseAllMatchesWithin7DaysSince(cricketMatchDAO.getMostRecentMatchDate().toLocalDate().plusDays(1));

            // insert the temp schema to DB
            uploadCricketMatchDataSchema(schema);
        } finally {
            System.out.println("Last 7 days updated");
        }

    }

    // handles the batching of the cricsheet ingestion due to its size (causing java heap errors)
    // instantiates the cricSheetParser to get a schema of the matches batch,
    // then calling this.uploadCricketMatchDataSchema() to handle the smart insert
    public void ingestCricSheet() {
        final int BATCH_SIZE = 500;
        CricSheetParser cricSheetParser = new CricSheetParser();
        List<String> allMatchIDs = cricSheetParser.getInternationalMatchIDs();
        int numMatches = allMatchIDs.size();

        for (int i = 0; i < numMatches; i += BATCH_SIZE) {
            int endIndex;
            if (BATCH_SIZE + i < allMatchIDs.size()) {
                endIndex = BATCH_SIZE + i;
            } else {
                endIndex = allMatchIDs.size();
            }
            Set<String> batchIDs = new HashSet<>(allMatchIDs.subList(i, endIndex));

            CricketMatchDataSchema schema = cricSheetParser.parseMatchesBatch(batchIDs);
            uploadCricketMatchDataSchema(schema);

            System.out.println((i+batchIDs.size()) + " out of " + numMatches + " parsed");
        }
    }

    // use bulk inserts to efficiently insert schema object into DB,
    // also handling complex Venue and Team de-duplication before inserting
    private void uploadCricketMatchDataSchema(CricketMatchDataSchema schema) {
        try (
                TeamDAO teamDAO = new TeamDAO();
                CricketMatchDAO matchDAO = new CricketMatchDAO();
                MatchResultDAO resultDAO = new MatchResultDAO();
                MatchTeamDAO matchTeamDAO = new MatchTeamDAO();
                VenueDAO venueDAO = new VenueDAO();
                VenueDeduplicator venueDeduplicator = new VenueDeduplicator()
        ) {
            // insert teams and venues
            TeamAliasMap teamAliasMap = new TeamAliasMap();
            for (Team team : schema.getTeams()) {
                if (!team.getName().equals(teamAliasMap.aliasCheck(team.getName()))) {
                    schema.replaceTeamNameEverywhere(teamAliasMap.aliasCheck(team.getName()), team.getName());
                }
            }
            teamDAO.bulkInsertIfNotExists(schema.getTeams());
            List<Team> teams = teamDAO.getIDsIntoObjects(schema.getTeams());
            for (Team team : teams) {
                schema.updateTeamKey(team);
            }

            Set<Venue> canonicalVenues = venueDAO.getAllVenues();
            Map<VenueKey, Venue> schemaKeyToCanonicalVenue = new HashMap<>();
            for (Venue v : schema.getVenues()) {

                boolean found = false;
                for (Venue c : new ArrayList<>(canonicalVenues)) {
                    if (venueDeduplicator.isSameVenue(v.getGroundName(), v.getCity(), c.getGroundName(), c.getCity())) {
                        if (venueDeduplicator.shouldReplace(v.getVenueKey(), c.getVenueKey())) {
                            canonicalVenues.remove(c);
                            canonicalVenues.add(new Venue(c.getVenueID(), v.getVenueKey()));
                            schemaKeyToCanonicalVenue.put(v.getVenueKey(), new Venue(c.getVenueID(), v.getVenueKey()));
                        } else {
                            schemaKeyToCanonicalVenue.put(v.getVenueKey(), c);
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // this will have an id of -1 for now
                    canonicalVenues.add(v);
                    schemaKeyToCanonicalVenue.put(v.getVenueKey(), v);
                }
            }
            // need to handle venue de duplication here
            venueDAO.bulkInsertAndUpdate(new ArrayList<>(canonicalVenues));
            // updateVenueKey uses natural venue key comparison in other tables and replaces the venueID in that table
            // so venues list must have the new ids but the old venue key before de duplication applied
            List<Venue> trueIDVenues = venueDAO.getIDsIntoObjects(new ArrayList<>(schemaKeyToCanonicalVenue.values()));
            // overwrite venue ids that have changed from getIdsIntoObjects()
            for (Venue listVenue : trueIDVenues) {
                for (Venue mapVenue : schemaKeyToCanonicalVenue.values()) {
                    if (listVenue.getVenueKey().equals(mapVenue.getVenueKey())) {
                        mapVenue.setVenueID(listVenue.getVenueID()); // override ID
                        break; // since map is exclusive, stop after match
                    }
                }
            }

            for (Map.Entry<VenueKey, Venue> entry : schemaKeyToCanonicalVenue.entrySet()) {
                schema.updateVenueKey(new Venue(entry.getValue().getVenueID(), entry.getKey()));
            }
            matchDAO.bulkInsertIfNotExists(schema.getCricketMatches());
            resultDAO.bulkInsertIfNotExists(schema.getMatchResults());
            matchTeamDAO.bulkInsertIfNotExists(schema.getMatchTeams());
        } catch (Exception ignored) {
        }
    }
}