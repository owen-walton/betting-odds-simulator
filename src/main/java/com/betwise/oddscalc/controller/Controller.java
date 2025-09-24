/**
 * Acts as a link between all service classes so logic can run together whilst maintaining separated
 * Each public function in controller is a different run case
 */

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
        ingestionService.ingestCountriesFromCricAPI(); // must ensure enough api hits available before this runs
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

    // TODO: create a use case that will test the accuracy of tuneModel()
    public void testModel(LocalDate trainingDataEndDateIncl) {
        // TuningService tuningService = new TuningService();
        // List<FactorApplication> factorApplications = tuningService.tuneModel(LocalDate.MIN, trainingDataEndDateIncl);

    }

    public BettingOdds findOdds(CricketMatchDataSchema match, PredictionModel model) {
        PredictionService predictionService = new PredictionService();
        Map<Team, Double> result = predictionService.predict(match, model);
        if (result == null) {
            System.out.println(match.getTeams().get(0) + " or " + match.getTeams().get(1) + " have not played enough matches to predict the result.");
            return null;
        }
        return new BettingOdds(result);
    }
}
