package com.betwise.oddscalc.main;


import com.betwise.oddscalc.controller.Controller;

import java.io.IOException;

public class MainApp {

    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();
        controller.maintainDatabase();
    }
}
