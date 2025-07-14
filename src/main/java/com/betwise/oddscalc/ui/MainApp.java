package com.betwise.oddscalc.ui;

import com.betwise.oddscalc.ingestdata.CricSheetParser;

public class MainApp {

    public static void main(String[] args)
    {
        CricSheetParser cricSheetParser = new CricSheetParser();

        System.out.println(cricSheetParser.getInternationalMatchIDs().size());
    }
}
