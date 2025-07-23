package com.betwise.oddscalc.service;

import com.betwise.oddscalc.database.dao.*;
import com.betwise.oddscalc.entity.CricketMatchDataSchema;
import com.betwise.oddscalc.entity.Team;
import com.betwise.oddscalc.entity.Venue;
import com.betwise.oddscalc.ingestdata.CricSheetParser;

import java.util.List;

public class IngestionService {
    public IngestionService() {

    }

    public void ingest() {
        CricSheetParser cricSheetParser = new CricSheetParser();
        uploadCricketMatchDataSchema(cricSheetParser.parseInternationalMatches());
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
            List<Team> teams = teamDAO.bulkInsertIfNotExists(schema.getTeams());
            for (Team team : teams) {
                schema.updateTeamKey(team);
            }
            List<Venue> venues = venueDAO.bulkInsertIfNotExists(schema.getVenues());
            for (Venue venue : venues) {
                schema.updateVenueKey(venue);
            }
            matchDAO.bulkInsertIfNotExists(schema.getCricketMatches());
            resultDAO.bulkInsertIfNotExists(schema.getMatchResults());
            matchTeamDAO.bulkInsertIfNotExists(schema.getMatchTeams());
        }
    }
}