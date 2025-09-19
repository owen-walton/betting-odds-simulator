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
    public void populateTeamHomeVenue() {
        try (TeamHomeVenueDAO teamHomeVenueDAO = new TeamHomeVenueDAO()) {
            teamHomeVenueDAO.populateTeamHomeVenue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void ingestCountriesFromCricAPI() {
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
        }
    }

    public Map<DataSource, String> updateLast7Days() throws IOException {
        try (
                TeamDAO teamDAO = new TeamDAO();
                CricketMatchDAO cricketMatchDAO = new CricketMatchDAO()
        ){
            CricAPIClient cricAPIClient = new CricAPIClient(new HTTPClient(), teamDAO.getAllTeamNames());

            CricketMatchDataSchema schema = cricAPIClient.parseAllMatchesWithin7DaysSince(cricketMatchDAO.getMostRecentMatchDate().toLocalDate().plusDays(1));

            Map<DataSource, String> newIDs = new HashMap<>();
            for (CricketMatch cm : schema.getCricketMatches()) {
                newIDs.put(cm.getDataSource(), cm.getMatchID());
            }
            uploadCricketMatchDataSchema(schema);

            // returns ids added so that the elo gain/loss can be calculated
            return newIDs;
        }

    }

    public void ingestCricSheet() {
        CricSheetParser cricSheetParser = new CricSheetParser();
        List<String> allMatchIDs = cricSheetParser.getInternationalMatchIDs();
        final int BATCH_SIZE = 500;

        for (int i = 0; i < allMatchIDs.size(); i += BATCH_SIZE) {
            int endIndex;
            if (BATCH_SIZE + i < allMatchIDs.size()) {
                endIndex = BATCH_SIZE + i;
            } else {
                endIndex = allMatchIDs.size();
            }
            Set<String> batchIDs = new HashSet<>(allMatchIDs.subList(i, endIndex));

            CricketMatchDataSchema schema = cricSheetParser.parseMatchesBatch(batchIDs);
            uploadCricketMatchDataSchema(schema);
        }
    }

    private void uploadCricketMatchDataSchema(CricketMatchDataSchema schema) {
        try (
                TeamDAO teamDAO = new TeamDAO();
                CricketMatchDAO matchDAO = new CricketMatchDAO();
                MatchResultDAO resultDAO = new MatchResultDAO();
                MatchTeamDAO matchTeamDAO = new MatchTeamDAO();
                VenueDAO venueDAO = new VenueDAO();
                VenueDeduplicator venueDeduplicator = new VenueDeduplicator(null)
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

    // bugged and fix was not findable so rewritten
    private CricketMatchDataSchema uploadVenues(CricketMatchDataSchema schema) {
        try (
                VenueDAO venueDAO = new VenueDAO();
                VenueDeduplicator venueDeduplicator = new VenueDeduplicator(initialiseCanonicalVenues(venueDAO))
        )
        {
            // get all venues to be added
            Set<Venue> venuesToAdd = new HashSet<>(schema.getVenues());

            // get trueIDs or -1 into venueIDs in venuesToAdd and update in memory copy of Venue table
            venuesToAdd = venueDeduplicator.updateCanonicalList(venuesToAdd);

            // add all new venues and edit all overwritten venues to db
            venueDAO.bulkInsertAndUpdate(new ArrayList<>(venueDeduplicator.getEditedAndNewVenues()));

            /*
            * - Get the new generated ids into the Venue objects that have a -1 id and call .updateVenueKey() on all.
            * - Do not need to get the new ids into the venueDuplicator.canonicalMap because it is use once per bulk
            * insert (not per single insert) so a new venueDuplicator will be redefined with newly correct map next
            * time used.
             */
            Set<Venue> requireID = new HashSet<>();
            Set<Venue> trueID = new HashSet<>();
            for (Venue v : venuesToAdd) {
                if (v.getVenueID() == -1) {
                    requireID.add(v);
                } else {
                    trueID.add(v);
                }
            }
            trueID.addAll(venueDAO.getIDsIntoObjects(new ArrayList<>(requireID)));
            for (Venue venue : trueID) {
                schema.updateVenueKey(venue);
            }

            return schema;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<VenueKey, VenueEditState> initialiseCanonicalVenues(VenueDAO venueDAO) {
        Map<VenueKey, VenueEditState> canonicalVenues = new HashMap<>();
        for (Venue v : venueDAO.getAllVenues()) {
            canonicalVenues.put(v.getVenueKey(), new VenueEditState(v.getVenueID(), false));
        }
        return canonicalVenues;
    }
}