package com.betwise.oddscalc.service;

import com.betwise.oddscalc.database.dao.*;
import com.betwise.oddscalc.entity.CricketMatchDataSchema;
import com.betwise.oddscalc.entity.Team;
import com.betwise.oddscalc.entity.Venue;
import com.betwise.oddscalc.ingestdata.CricSheetParser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                VenueDAO venueDAO = new VenueDAO();
                // TeamHomeVenueDAO homeDAO = new TeamHomeVenueDAO();
                // team home venue requires calculation from entire dataset so is not derived purely from schema object
                // will be calculated later
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
            venueDAO.bulkInsertIfNotExists(schema.getVenues());
            List<Venue> venues = venueDAO.getIDsIntoObjects(schema.getVenues());
            for (Venue venue : venues) {
                schema.updateVenueKey(venue);
            }

            matchDAO.bulkInsertIfNotExists(schema.getCricketMatches());
            resultDAO.bulkInsertIfNotExists(schema.getMatchResults());
            matchTeamDAO.bulkInsertIfNotExists(schema.getMatchTeams());
        }
    }
}