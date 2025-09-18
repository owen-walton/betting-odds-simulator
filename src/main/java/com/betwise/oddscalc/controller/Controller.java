package com.betwise.oddscalc.controller;

import com.betwise.oddscalc.database.initialise.DatabaseInitialiser;
import com.betwise.oddscalc.entity.CricketMatchDataSchema;
import com.betwise.oddscalc.entity.FactorApplication;
import com.betwise.oddscalc.service.IngestionService;
import com.betwise.oddscalc.service.TuningService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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

    public void maintainDatabase() throws IOException {
        IngestionService ingestionService = new IngestionService();
        ingestionService.updateLast7Days();
        ingestionService.populateTeamHomeVenue();
    }

    // updates model to match the latest version of cricket match data
    public void updatePredictionModel() {
        TuningService tuningService = new TuningService();
        tuningService.updatePredictionModel();
    }

    //------------------------------------------------------------------------------
    //------------------------------------------------------------------------------
    //------------------------------------------------------------------------------

    public void testModel(LocalDate trainingDataEndDateIncl) {
        TuningService tuningService = new TuningService();
        // List<FactorApplication> factorApplications = tuningService.tuneModel(LocalDate.MIN, trainingDataEndDateIncl);

    }

    public void findOdds(CricketMatchDataSchema match) {

    }
}
