package com.betwise.oddscalc.controller;

import com.betwise.oddscalc.database.initialise.DatabaseInitialiser;
import com.betwise.oddscalc.service.IngestionService;

import java.io.IOException;

public class Controller {
    public void init() throws IOException {
        IngestionService ingestionService = new IngestionService();
        DatabaseInitialiser databaseInitialiser = new DatabaseInitialiser();
        databaseInitialiser.runDDL();
        ingestionService.ingestCricSheet();
        ingestionService.ingestCountriesFromCricAPI(); // ensure enough api hits available
        ingestionService.updateLast7Days();
        ingestionService.populateTeamHomeVenue();
    }

    public void maintain() throws IOException {
        IngestionService ingestionService = new IngestionService();
        ingestionService.updateLast7Days();
        ingestionService.populateTeamHomeVenue();
    }
}
