package com.betwise.oddscalc.ingestdata;


import com.betwise.oddscalc.entity.Match;
import com.betwise.oddscalc.ingestdata.ingestutils.FileReadHelper;

import java.util.ArrayList;
import java.util.List;

public class CricSheetParser {

    private final String CRICSHEET_PATH = "cricsheet/cricsheet.zip";
    private final String README_NAME = "README.txt";
    private final String JSON_EXTENSION = ".json";

    public CricSheetParser() {

    }

    public void parseInternationalMatches() {

        List<String> internationalMatchIDs = getInternationalMatchIDs();

        for (String matchID : internationalMatchIDs) {

        }
    }

    public Match getMatchData(String matchID) {

        List<String> matchJsonAsList = FileReadHelper.readZipFromResources(CRICSHEET_PATH, matchID + JSON_EXTENSION);
        String szMatchJson = joinStringList(matchJsonAsList);
        Match match = null;

        return match;
    }

    public String joinStringList(List<String> list) {
        StringBuilder joinedStr = new StringBuilder();

        for (String str : list) {
            joinedStr.append(str);
        }

        return joinedStr.toString();
    }

    public List<String> getInternationalMatchIDs() {
        boolean startReading = false;
        List<String> readmeText = FileReadHelper.readZipFromResources(CRICSHEET_PATH, README_NAME);
        List<String> internationalMatchIDs = new ArrayList<>();

        for (String line : readmeText) {

            // remove prose at start of readme text
            if (!startReading) {
                // matches are stored with date at start so don't begin reading until that format is reached
                if (line.matches("^\\d{4}-\\d{2}-\\d{2} - .*")) {
                    startReading = true;
                } else {
                    continue;
                }
            }

            // continue statement means this is only ran once matches are reached
            String[] parts = line.split(" - ");
            // protect from index out of bounds error against erroneous data
            if (parts.length >= 5) {
                String type = parts[1].trim();
                String matchID = parts[4].trim();
                if (type.equalsIgnoreCase("international")) {
                    internationalMatchIDs.add(matchID);
                }
            }
        }

        return internationalMatchIDs;
    }
}
