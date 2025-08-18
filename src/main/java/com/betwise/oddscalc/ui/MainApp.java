package com.betwise.oddscalc.ui;


import com.betwise.oddscalc.ingestdata.CricAPIClient;
import com.betwise.oddscalc.ingestdata.ingestutils.HTTPClient;
import com.betwise.oddscalc.service.IngestionService;

public class MainApp {

    public static void main(String[] args) {
        // IngestionService ingestionService = new IngestionService();
        // ingestionService.ingest();
        CricAPIClient cricAPIClient = new CricAPIClient(new HTTPClient());
        cricAPIClient.getMatchDetailsJson();
    }
}
