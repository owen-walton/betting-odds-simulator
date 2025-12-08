/**
 * @author Owen Walton
 *
 * Handles all parsing of local Cricsheet JSON data.
 * This class reads the cricsheet.zip file from resources, extracts the required match JSON files,
 * parses them using ParseJSON.java into nested maps, then manually converts them into the container objects
 * ,
 * The structure/output is intentionally made consistent with CricAPIClient so that
 * IngestionService can process both sources identically.
 */

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

    // parses a batch of matches based on matchIDs:
    // reads all required JSON files from cricsheet.zip,
    // parses each match individually,
    // and merges the results into one CricketMatchDataSchema.
    // duplicates are handled later by DAOs not parser
    public CricketMatchDataSchema parseMatchesBatch(Set<String> matchIDs) {
        CricketMatchDataSchema internationalCricketData = new CricketMatchDataSchema();

        Map<String, List<String>> allMatchJsons = FileReadHelper.readZipFilesFromResources(CRICSHEET_PATH, new HashSet<>(matchIDs), JSON_EXTENSION);
        for (String matchID : matchIDs) {
            internationalCricketData.appendSchema(
                    parseSingleMatch(matchID, allMatchJsons)
            );
        }

        return internationalCricketData;
    }

    // parses a single match:
    // joins the JSON file lines, parses JSON to a map using helper,
    // verifies match format is official Test/T20/ODI (Cricsheet sometimes stores unofficial "4 day match", etc.),
    // builds all container objects from parsed map and stores them in a CricketMatchDataSchema container.
    // If a match is unofficial or missing required fields, an empty schema is returned.
    private CricketMatchDataSchema parseSingleMatch(String matchID, Map<String, List<String>> allMatchJsons) {

        Map<String, Object> matchInfoMap = ParseJSON.parseJsonToMap(
                joinStringList(allMatchJsons.get(matchID)), Set.of("innings", "meta")
        );

        // other formats like IT20 and ODM are unofficial matches which are to be disregarded and not returned
        // this is done at start of method to save unnecessary computation if returning nothing
        // also disregard women's matches
        String matchFormat = (String) ParseJSON.getValueFromMap("info/match_type", matchInfoMap);
        String gender = (String) ParseJSON.getValueFromMap("info/gender", matchInfoMap);
        String event = (String) ParseJSON.getValueFromMap("info/event/name", matchInfoMap);

        if ((gender != null && (gender.equalsIgnoreCase("female") ||
                gender.equalsIgnoreCase("women")))
                || event != null && event.toLowerCase().contains("women")) {
            return new CricketMatchDataSchema();
        }
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
            teams.add(new Team(-1, name));
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
        TeamKey tossWinningTeamKey = new TeamKey(
                Normaliser.normalise(
                        (String)ParseJSON.getValueFromMap("info/toss/winner", matchInfoMap)
                )
        );
        TossDecision tossDecision = switch (
                (String) ParseJSON.getValueFromMap ("info/toss/decision", matchInfoMap)
                ) {
            case "bat" -> TossDecision.BAT;
            case "field" -> TossDecision.FIELD;
            default -> {
                System.out.println( (String) ParseJSON.getValueFromMap ("info/toss/decision", matchInfoMap)
                        + " is not a valid toss decision.");
                throw new RuntimeException();
            }
        };

        String strResult = (String) ParseJSON.getValueFromMap( "info/outcome/result", matchInfoMap );
        Result result;
        TeamKey winningTeamKey;
        if (strResult == null) {
            result = Result.WIN;
            winningTeamKey = new TeamKey(
                    Normaliser.normalise(
                            (String)ParseJSON.getValueFromMap("info/outcome/winner", matchInfoMap)
                    )
            );
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
            if (ParseJSON.getValueFromMap(
                    "info/outcome/by/innings", matchInfoMap
            ) != null) {
                marginSize = (Integer) ParseJSON.getValueFromMap(
                        "info/outcome/by/runs", matchInfoMap
                );
                marginType = MarginType.ONE_INNINGS_AND_RUNS;
            } else {
                Integer wickets = (Integer) ParseJSON.getValueFromMap(
                        "info/outcome/by/wickets", matchInfoMap
                );
                if (wickets != null) {
                    marginSize = wickets;
                    marginType = MarginType.WICKETS;
                } else {
                    marginSize = (Integer) ParseJSON.getValueFromMap(
                            "info/outcome/by/runs", matchInfoMap
                    );
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
                -1,
                -1,
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
                -1,
                matchFormat,
                venueKey
        );

        // get match teams
        List<MatchTeam> matchTeams = new ArrayList<>();
        matchTeams.add(new MatchTeam(-1, matchID, DataSource.CRICSHEET, -1, new TeamKey(teams.get(0).getName())));
        matchTeams.add(new MatchTeam(-1, matchID, DataSource.CRICSHEET, -1, new TeamKey(teams.get(1).getName())));

        // build all information about match into schema object
        CricketMatchDataSchema tempSchema = new CricketMatchDataSchema(
                teams,
                List.of(new Venue(-1, venueKey)),
                null,
                List.of(matchResult),
                List.of(match),
                matchTeams
        );
        return tempSchema;
    }

    // helper to join a list of strings in order into one string
    private String joinStringList(List<String> list) {
        StringBuilder joinedStr = new StringBuilder();

        for (String str : list) {
            joinedStr.append(str);
        }

        return joinedStr.toString();
    }

    // whilst each json match file has a match_type field for 'international' or 'club',
    // this would require the reading and decompression of every single json file despite only some being used/stored
    // which is expensive, instead the readme is used to find all ids of files that require reading from
    // method reads in the readme from the zip using FileReadHelper class, then gets all ids that say 'international'
    // this also allows IngestionService.java to batch the ingestion
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
