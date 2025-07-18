package com.betwise.oddscalc.ingestdata;


import com.betwise.oddscalc.entity.Match;
import com.betwise.oddscalc.entity.MatchFormat;
import com.betwise.oddscalc.entity.Venue;
import com.betwise.oddscalc.ingestdata.ingestutils.FileReadHelper;
import com.betwise.oddscalc.ingestdata.ingestutils.ParseJSON;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CricSheetParser {

    private final String CRICSHEET_PATH = "cricsheet/cricsheet.zip";
    private final String README_NAME = "README.txt";
    private final String JSON_EXTENSION = ".json";
    private final List<Venue> venueList = new ArrayList<>();

    public CricSheetParser() {

    }

    public void parseInternationalMatches() {

        List<String> internationalMatchIDs = getInternationalMatchIDs();
        List<Match> internationalMatches = new ArrayList<>();
        ParseJSON jsonParser = new ParseJSON();

        for (String matchID : internationalMatchIDs) {
            internationalMatches.add(getMatchData(matchID, jsonParser));
        }
    }

    public Match getMatchData(String matchID, ParseJSON jsonParser) {

        String szMatchJson = joinStringList(FileReadHelper.readZipFromResources(CRICSHEET_PATH, matchID + JSON_EXTENSION));
        Map<String, Object> matchMap = jsonParser.parseJsonToMap(szMatchJson, Set.of("innings"));

        List<String> matchDates = (List<String>) jsonParser.getValueFromMap("info/dates", matchMap);
        String[] venue = ((String) jsonParser.getValueFromMap("info/venue", matchMap)).split(",");
        String ground = venue[0].trim();
        String city;

        // if venue field is in format "Ground name, City"
        if(venue.length == 2) {
            city = venue[1].trim();
        } else { // otherwise just take city from
            city = (String) jsonParser.getValueFromMap("info/city", matchMap);
        }

        Match match = new Match(
                MatchFormat.fromString((String) jsonParser.getValueFromMap("info/match_type", matchMap)),
                LocalDate.parse(matchDates.get(0)),
                matchDates.size(),
                queryVenueList(ground, city),
                null,
                null
        );

        return match;
    }

    public String joinStringList(List<String> list) {
        StringBuilder joinedStr = new StringBuilder();

        for (String str : list) {
            joinedStr.append(str);
        }

        return joinedStr.toString();
    }

    // whilst each json match file has a match_type field for 'international' or 'club',
    // this would require the reading and decompression of every single json file despite only some being used/stored
    // which is expensive, instead the readme is used to find all ids of files that require reading from
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

    public Venue queryVenueList(String ground, String city) {
        if (ground == null || city == null || ground.isBlank() || city.isBlank()) {
            return null;
        }
        // to combat inconsistencies with cricsheet venues as names of same grounds sometimes vary
        String normalisedGround = normaliseString(ground);
        String normalisedCity = normaliseString(city);

        for (Venue venue : venueList) {

            // if the venue already exists (ignoring casing, punctuation, etc) then return that venue
            if (normaliseString(venue.ground()).equals(normalisedGround)
                    && normaliseString(venue.city()).equals(normalisedCity)) {
                return venue;
            }
        }

        // venue is not found so add to list and return
        Venue newVenue = new Venue(formatForStorage(ground), formatForStorage(city));
        venueList.add(newVenue);
        return newVenue;
    }

    public String normaliseString(String str) {
        return str.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public String formatForStorage(String input) {
        if (input == null || input.isBlank()) return "";

        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();

        // make word title case
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
