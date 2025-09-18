package com.betwise.oddscalc.service;

import com.betwise.oddscalc.database.dao.FactorApplicationDAO;
import com.betwise.oddscalc.entity.FactorApplication;

import java.time.LocalDate;
import java.util.List;

public class TuningService {
    public List<FactorApplication> tuneModel(LocalDate dataStartDate, LocalDate dataEndDate) {
        // TODO
    }

    // wrapper for tuneModel that uses entire data set
    public void updatePredictionModel() {
        try (FactorApplicationDAO factorApplicationDAO = new FactorApplicationDAO()) {
            factorApplicationDAO.clear();
            factorApplicationDAO.bulkUpsertIfNotExists(tuneModel(LocalDate.MIN, LocalDate.MAX));
        }

    }
}
