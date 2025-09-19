package com.betwise.oddscalc.controller;

import com.betwise.oddscalc.database.initialise.DatabaseInitialiser;
import com.betwise.oddscalc.entity.*;
import com.betwise.oddscalc.service.IngestionService;
import com.betwise.oddscalc.service.OddsService;
import com.betwise.oddscalc.service.PredictionService;
import com.betwise.oddscalc.service.TuningService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

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
        PredictionService predictionService = new PredictionService();
        Map<DataSource, String> idsAdded = ingestionService.updateLast7Days();
        ingestionService.populateTeamHomeVenue();
        predictionService.updateELOsFor(idsAdded);
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

    private BettingOdds findOdds(CricketMatchDataSchema match, PredictionModel model) {
        PredictionService predictionService = new PredictionService();
        Map<Team, Double> result = predictionService.predict(match, model);
        return new BettingOdds(result);
    }
}
