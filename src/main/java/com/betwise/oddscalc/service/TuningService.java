package com.betwise.oddscalc.service;

import com.betwise.oddscalc.database.dao.PredictionModelDAO;
import com.betwise.oddscalc.entity.PredictionModel;
import com.betwise.oddscalc.entity.TuningParams;

public class TuningService {

    // wrapper for tuneModel that uses entire data set
    public void updatePredictionModel() {
        try (PredictionModelDAO predictionModelDAO = new PredictionModelDAO()) {
            predictionModelDAO.insert(tuneModel());
        }
    }

    // inserts team's up to date tuned ELOs into the database
    // returns the prediction model that should be used with the ELOs for calculation
    public PredictionModel tuneModel() {
        return null;
    }
}
