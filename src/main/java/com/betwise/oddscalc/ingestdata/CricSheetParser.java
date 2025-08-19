package com.betwise.oddscalc.ingestdata;


import com.betwise.oddscalc.entity.*;
import com.betwise.oddscalc.ingestdata.ingestutils.FileReadHelper;
import com.betwise.oddscalc.ingestdata.ingestutils.ParseJSON;
import com.betwise.oddscalc.ingestdata.ingestutils.Normaliser;

import java.time.LocalDate;
import java.util.*;

public class CricSheetParser {

    private final String CRICSHEET_PATH = "cricsheet/cricsheet.zip";
    private final String README_NAME = "README.txt";
    private final String JSON_EXTENSION = ".json";
    private final List<Venue> venueList = new ArrayList<>();

    // duplicates are handled later by DAOs not parser
    public CricketMatchDataSchema parseMatchesBatch(Set<String> matchIDs) {
        CricketMatchDataSchema internationalCricketData = new CricketMatchDataSchema();

        double index = 0.0;
        double size = matchIDs.size();
        Map<String, List<String>> allMatchJsons = FileReadHelper.readZipFilesFromResources(CRICSHEET_PATH, new HashSet<>(matchIDs), JSON_EXTENSION);
        for (String matchID : matchIDs) {
            long start = System.currentTimeMillis();

            internationalCricketData.appendSchema(parseSingleMatch(matchID, allMatchJsons));

            long end = System.currentTimeMillis();
            System.out.println("Parsed in " + (end - start) + "ms");
            index = index + 1;
            System.out.println("Batch " + (index / size) * 100.0 + "% complete");
        }

        return internationalCricketData;
    }

    public CricketMatchDataSchema parseSingleMatch(String matchID, Map<String, List<String>> allMatchJsons) {

        Map<String, Object> matchInfoMap = ParseJSON.parseJsonToMap(joinStringList(allMatchJsons.get(matchID)), Set.of("innings", "meta"));

        // other formats like IT20 and ODM are unofficial matches which are to be disregarded and not returned
        // this is done at start of method to save unnecessary computation if returning nothing
        String matchFormat = (String) ParseJSON.getValueFromMap("info/match_type", matchInfoMap);
        if (matchFormat == null ||
                (!matchFormat.equalsIgnoreCase("Test") &&
                !matchFormat.equalsIgnoreCase("T20") &&
                !matchFormat.equalsIgnoreCase("ODI"))) {
            return new CricketMatchDataSchema();
        }

        // get teams
        List<String> teamNames = (List<String>) ParseJSON.getValueFromMap("info/teams", matchInfoMap);
        List<Team> teams = new ArrayList<>();
        for (String name : teamNames) {
            teams.add(new Team(0, name));
        }

        // get venues
        String[] venue = ((String) ParseJSON.getValueFromMap("info/venue", matchInfoMap)).split(",");
        String ground = venue[0].trim();
        String city;
        // if venue field is in format "Ground name, City"
        if(venue.length == 2) {
            city = venue[1].trim();
        } else { // otherwise just take city from
            city = (String) ParseJSON.getValueFromMap("info/city", matchInfoMap);
            if (city == null) {
                city = "";
            }
        }
        VenueKey venueKey = Normaliser.normaliseVenueKey(new VenueKey(ground, city));

        // get match results
        TeamKey tossWinningTeamKey = new TeamKey((String)ParseJSON.getValueFromMap("info/toss/winner", matchInfoMap));
        TossDecision tossDecision = switch ((String)ParseJSON.getValueFromMap("info/toss/decision", matchInfoMap)) {
            case "bat" -> TossDecision.BAT;
            case "field" -> TossDecision.FIELD;
            default -> {
                System.out.println((String)ParseJSON.getValueFromMap("info/toss/decision", matchInfoMap)
                        + " is not a valid toss decision.");
                throw new RuntimeException();
            }
        };

        String strResult = (String)ParseJSON.getValueFromMap("info/outcome/result", matchInfoMap);
        Result result;
        TeamKey winningTeamKey;
        if (strResult == null) {
            result = Result.WIN;
            winningTeamKey = new TeamKey((String)ParseJSON.getValueFromMap("info/outcome/winner", matchInfoMap));
        } else {
            if (strResult.equalsIgnoreCase("tie")) {
                String winner = (String) ParseJSON.getValueFromMap("info/outcome/eliminator", matchInfoMap);
                result = Result.WIN_IN_SUPER_OVER;
                if (winner == null) {
                    winner = (String) ParseJSON.getValueFromMap("info/outcome/bowl_out", matchInfoMap);
                    result = Result.WIN_IN_BOWL_OFF;
                }
                if (winner == null) {
                    result = Result.TIE;
                } else {
                    winningTeamKey = new TeamKey(winner);
                }
            } else {
                result = Result.fromString(strResult);
                winningTeamKey = null;
            }
            winningTeamKey = null;
        }

        Integer marginSize;
        MarginType marginType;
        if (result == Result.WIN) {
            if (((ParseJSON.getValueFromMap("info/outcome/by/innings", matchInfoMap))) != null) {
                marginSize = (Integer)(ParseJSON.getValueFromMap("info/outcome/by/runs", matchInfoMap));
                marginType = MarginType.ONE_INNINGS_AND_RUNS;
            } else {
                Integer wickets = (Integer)(ParseJSON.getValueFromMap("info/outcome/by/wickets", matchInfoMap));
                if (wickets != null) {
                    marginSize = wickets;
                    marginType = MarginType.WICKETS;
                } else {
                    marginSize = (Integer)(ParseJSON.getValueFromMap("info/outcome/by/runs", matchInfoMap));
                    marginType = MarginType.RUNS;
                }
            }
        } else {
            marginSize = null;
            marginType = null;
        }

        MatchResult matchResult = new MatchResult(
                matchID,
                DataSource.CRICSHEET,
                0,
                0,
                tossDecision,
                marginSize,
                marginType,
                result,
                winningTeamKey,
                tossWinningTeamKey
        );

        // get match
        List<String> matchDates = (List<String>) ParseJSON.getValueFromMap("info/dates", matchInfoMap);
        CricketMatch match = new CricketMatch(
                matchID,
                DataSource.CRICSHEET,
                LocalDate.parse(matchDates.get(0)),
                0,
                matchFormat,
                venueKey
        );

        // get match teams
        List<MatchTeam> matchTeams = new ArrayList<>();
        matchTeams.add(new MatchTeam(0, matchID, DataSource.CRICSHEET, 0, new TeamKey(teams.get(0).getName())));
        matchTeams.add(new MatchTeam(0, matchID, DataSource.CRICSHEET, 0, new TeamKey(teams.get(1).getName())));

        // build all information about match into schema object
        CricketMatchDataSchema tempSchema = new CricketMatchDataSchema(
                null, // match formats are added in DDL so doesn't matter
                teams,
                List.of(new Venue(0, venueKey)),
                null,
                List.of(matchResult),
                List.of(match),
                matchTeams
        );
        return tempSchema;
    }

    // unfinished at determining home team
    public Map<String, HomeStatus> getTeamsAndHomeStatus(Map<String, Object> matchMap) {

        Map<String, HomeStatus> teamHomeStatusMap = new HashMap<>();
        String[] teams = ((List<String>)ParseJSON.getValueFromMap("info/teams", matchMap)).toArray(new String[0]);

        // if event is a tour, home team is implied
        String eventName = ((String)ParseJSON.getValueFromMap("info/event/name", matchMap));
        if (eventName.contains(" tour of ")) {
            if (eventName.split(" ")[0].trim().equalsIgnoreCase(teams[0].trim())) {
                teamHomeStatusMap.put(teams[0], HomeStatus.HOME);
                teamHomeStatusMap.put(teams[1], HomeStatus.AWAY);
                return teamHomeStatusMap;
            } else if (eventName.split(" ")[0].equalsIgnoreCase(teams[1])) {
                teamHomeStatusMap.put(teams[1], HomeStatus.HOME);
                teamHomeStatusMap.put(teams[0], HomeStatus.AWAY);
                return teamHomeStatusMap;
            }
        }
        return null;
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
}
