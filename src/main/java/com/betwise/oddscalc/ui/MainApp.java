package com.betwise.oddscalc.ui;


import com.betwise.oddscalc.database.dao.TeamDAO;
import com.betwise.oddscalc.ingestdata.CricAPIClient;
import com.betwise.oddscalc.ingestdata.ingestutils.HTTPClient;
import com.betwise.oddscalc.service.IngestionService;

import java.io.IOException;
import java.time.LocalDate;

public class MainApp {

    public static void main(String[] args) throws IOException {
        IngestionService ingestionService = new IngestionService();
        CricAPIClient cricAPIClient = new CricAPIClient(new HTTPClient(), new TeamDAO().getAllTeamNames());
        // ingestionService.ingestCountriesFromCricAPI();
        ingestionService.ingestCricSheet();
        // ingestionService.updateLast7Days();
    }
}
