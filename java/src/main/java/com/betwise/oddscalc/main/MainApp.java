package com.betwise.oddscalc.main;


import com.betwise.oddscalc.controller.Controller;
import com.betwise.oddscalc.database.initialise.DatabaseInitialiser;
import com.betwise.oddscalc.service.IngestionService;

import java.io.IOException;

public class MainApp {

    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();

        controller.init();
    }
}
