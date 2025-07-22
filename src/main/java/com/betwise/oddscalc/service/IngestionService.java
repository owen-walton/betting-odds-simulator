package com.betwise.oddscalc.service;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.database.dao.*;
import com.betwise.oddscalc.entity.CricketMatchDataSchema;
import com.betwise.oddscalc.ingestdata.CricSheetParser;

public class IngestionService {
    public IngestionService() {

    }

    public void ingest() {
        CricketMatchDataSchema schema;
        uploadCricketMatchDataSchema(schema);
    }

    public void uploadCricketMatchDataSchema(CricketMatchDataSchema schema) {
        try (
                TeamDAO teamDAO = new TeamDAO();
                VenueDAO venueDAO = new VenueDAO();
                // TeamHomeVenueDAO homeDAO = new TeamHomeVenueDAO();
                // team home venue requires calculation from entire dataset so is not derived purely from schema object
                CricketMatchDAO matchDAO = new CricketMatchDAO();
                MatchResultDAO resultDAO = new MatchResultDAO();
                MatchTeamDAO matchTeamDAO = new MatchTeamDAO()
        ) {
            teamDAO.bulkInsertIfNotExists(schema.teams());
            venueDAO.bulkInsertIfNotExists(schema.venues());
            matchDAO.bulkInsertIfNotExists(schema.cricketMatches());
            resultDAO.bulkInsertIfNotExists(schema.matchResults());
            matchTeamDAO.bulkInsertIfNotExists(schema.matchTeams());
        }
    }

    public void loadCricsheet() {
        CricSheetParser cricSheetParser = new CricSheetParser();
        cricSheetParser.parseInternationalMatches();
    }
}