package com.betwise.oddscalc.service;

import com.betwise.oddscalc.database.dao.*;
import com.betwise.oddscalc.entity.*;
import com.betwise.oddscalc.ingestdata.CricSheetParser;
import com.betwise.oddscalc.ingestdata.ingestutils.VenueDeduplicator;

import java.util.*;

public class IngestionService {
    public IngestionService() {

    }

    public void ingest() {
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

    public void uploadCricketMatchDataSchema(CricketMatchDataSchema schema) {
        try (
                TeamDAO teamDAO = new TeamDAO();
                TeamHomeVenueDAO homeDAO = new TeamHomeVenueDAO();
                CricketMatchDAO matchDAO = new CricketMatchDAO();
                MatchResultDAO resultDAO = new MatchResultDAO();
                MatchTeamDAO matchTeamDAO = new MatchTeamDAO()
        ) {
            // insert teams and venues
            teamDAO.bulkInsertIfNotExists(schema.getTeams());
            List<Team> teams = teamDAO.getIDsIntoObjects(schema.getTeams());
            for (Team team : teams) {
                schema.updateTeamKey(team);
            }
            schema = uploadVenues(schema);
            homeDAO.bulkInsertIfNotExists(schema.getTeamHomeVenues());
            matchDAO.bulkInsertIfNotExists(schema.getCricketMatches());
            resultDAO.bulkInsertIfNotExists(schema.getMatchResults());
            matchTeamDAO.bulkInsertIfNotExists(schema.getMatchTeams());
        }
    }

    public CricketMatchDataSchema uploadVenues(CricketMatchDataSchema schema) {
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