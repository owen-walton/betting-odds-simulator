package com.betwise.oddscalc.ui;


import com.betwise.oddscalc.database.dao.TeamDAO;
import com.betwise.oddscalc.ingestdata.CricAPIClient;
import com.betwise.oddscalc.ingestdata.ingestutils.HTTPClient;
import com.betwise.oddscalc.service.IngestionService;

import java.io.IOException;

public class MainApp {

    public static void main(String[] args) throws IOException {
        IngestionService ingestionService = new IngestionService();
        // ingestionService.ingestCountriesFromCricAPI();
        // ingestionService.ingestCricSheet();
        ingestionService.updateLast7Days();
    }
}
