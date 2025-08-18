package com.betwise.oddscalc.ingestdata;

import com.betwise.oddscalc.entity.*;
import com.betwise.oddscalc.ingestdata.ingestutils.HTTPClient;
import com.betwise.oddscalc.ingestdata.ingestutils.ParseJSON;
import com.betwise.oddscalc.ingestdata.ingestutils.Normaliser;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class CricAPIClient {
    private final HTTPClient httpClient;
    private final String apiKey;
    private final String BASE_URL = "https://api.cricapi.com/v1";

    public CricAPIClient(HTTPClient httpClient) {
        this.httpClient = httpClient;
        this.apiKey = extractApiKey();
    }

    public String extractApiKey() {
        Properties cricapiProps = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("cricapi/cricapi.properties")) {

            if (input == null) {
                throw new RuntimeException("Can't find cricapi/cricapi.properties in resources");
            }

            cricapiProps.load(input);
            return cricapiProps.getProperty("apikey");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // not enough data from initial get, matchID is field of interest
    public String getMatchListJson() throws IOException {
        String url = String.format("%s/matches?apikey=%s&international=true&gender=male", BASE_URL, apiKey);
        return httpClient.get(url);
    }

    public Set<String> getCompletedMatchIdsSince(LocalDate fromDate) throws IOException {
        // fetch all matches
        Map<String, Object> jsonMap = ParseJSON.parseJsonToMap(getMatchListJson());
        List<Map<String, Object>> matches = (List<Map<String, Object>>) jsonMap.get("data");

        if (matches == null) {
            throw new RuntimeException("No matches returned by API");
        }

        Set<String> idSet = new HashSet<>();
        for (Map<String, Object> match : matches) {
            String status = (String) match.get("status");
            String dateStr = (String) match.get("date");
            LocalDate matchDate = LocalDate.parse(dateStr);

            // only include completed matches after fromDate
            if ("Completed".equalsIgnoreCase(status) && !matchDate.isBefore(fromDate)) {
                idSet.add((String) match.get("id"));
            }
        }
        return idSet;
    }

    // fetches the detailed match info by matchID
    public String getMatchDetailsJson(String matchID) throws IOException {
        String url = String.format("%s/match_info?apikey=%s&id=%s", BASE_URL, apiKey, matchID);
        return httpClient.get(url);
    }

    @SuppressWarnings("unchecked")
    public CricketMatchDataSchema parseAllMatchesSince(LocalDate fromDate) throws IOException {
        // add all matchIDs of finished matches after fromDate
        Set<String> idSet = getCompletedMatchIdsSince(fromDate);

        // use matchID set to parse all matches
        CricketMatchDataSchema schema = new CricketMatchDataSchema();
        for (String matchID : idSet) {
            schema.appendSchema(parseSingleMatch(matchID, fromDate));
        }
        return schema;
    }

    public CricketMatchDataSchema parseSingleMatch(String matchID, LocalDate fromDate) throws IOException {
        // get json
        String matchJson = getMatchDetailsJson(matchID);

        // null check
        if (matchJson == null) {
            throw new RuntimeException("Error getting match details");
        }

        // get map of match details
        Map<String, Object> matchInfoMap = ParseJSON.parseJsonToMap(matchJson);

        // double check match is completed and correct date
        String status = (String) ParseJSON.getValueFromMap("data/status", matchInfoMap);
        String dateStr = (String) ParseJSON.getValueFromMap("data/date", matchInfoMap);
        // dateStr already assigned earlier in method for a safety check
        String dtGmt = (String) ParseJSON.getValueFromMap("data/dateTimeGMT", matchInfoMap);
        if (dtGmt != null && dtGmt.length() >= 10) {
            dateStr = dtGmt.substring(0, 10);
        }
        if (status == null || dateStr == null) {
            throw new RuntimeException("Missing status or date in json");
        }
        LocalDate matchDate = LocalDate.parse(dateStr);
        if (!"Completed".equalsIgnoreCase(status) || matchDate.isBefore(fromDate)) {
            return new CricketMatchDataSchema(); // skip match
        }

        // get match format
        String format = (String) ParseJSON.getValueFromMap("data/matchType", matchInfoMap);
        format = format.toLowerCase(); // for the comparison
        if (format.equals("t20") || format.equals("t20i")) {
            format = "T20";
        } else if (format.equals("odi")) {
            format = "ODI";
        } else if (format.equals("test")) {
            format = "Test";
        } else {
            // skip the match
            return new CricketMatchDataSchema();
        }

        // get teams
        List<Team> teams = new ArrayList<>();
        String homeTeamName = ((List<String>) ParseJSON.getValueFromMap("data/teams", matchInfoMap)).get(0);
        String awayTeamName = ((List<String>) ParseJSON.getValueFromMap("data/teams", matchInfoMap)).get(1);
        teams.add(new Team(0, homeTeamName));
        teams.add(new Team(0, awayTeamName));
        if (homeTeamName == null || awayTeamName == null) {
            return new CricketMatchDataSchema();
        }

        // get venue
        String venueStr = (String)ParseJSON.getValueFromMap("data/venue", matchInfoMap);
        String ground = venueStr.split(",", 2)[0];
        String city = venueStr.split(",", 2)[1];
        VenueKey venueKey = Normaliser.normaliseVenueKey(new VenueKey(ground, city));

        // get result
        String toss = (String) ParseJSON.getValueFromMap("data/tossChoice", matchInfoMap);
        TossDecision tossDecision =
                switch (toss == null ? "" : toss.toLowerCase()) {
            case "bat" -> TossDecision.BAT;
            case "field" -> TossDecision.FIELD;
            default -> {
                throw new RuntimeException("Invalid toss decision");
            }
        };

        String winner = (String) ParseJSON.getValueFromMap("data/winner", matchInfoMap);
        String statusNote = (String) ParseJSON.getValueFromMap("data/statusNote", matchInfoMap);
        Result result;
        TeamKey winningTeamKey = null;
        Integer marginSize = null;
        MarginType marginType = null;

        if (winner != null && !winner.isBlank()) {
            winningTeamKey = new TeamKey(winner);
            result = Result.WIN;

            if (statusNote != null) {
                String note = statusNote.toLowerCase();
                if (note.contains("wicket")) {
                    try {
                        marginSize = Integer.parseInt(statusNote.replaceAll("[^0-9]", ""));
                        marginType = MarginType.WICKETS;
                    } catch (Exception ignored) {}
                } else if (note.contains("run")) {
                    try {
                        marginSize = Integer.parseInt(statusNote.replaceAll("[^0-9]", ""));
                        marginType = MarginType.RUNS;
                    } catch (Exception ignored) {}
                }
                // fallback for DLS or unknown win margins
                if (marginType == null) {
                    marginType = MarginType.UNKNOWN;
                }
            }
        } else if (statusNote != null) {
            if (statusNote.toLowerCase().contains("tie")) {
                result = Result.TIE;
            } else if (statusNote.toLowerCase().contains("no result")) {
                result = Result.NO_RESULT;
            } else if (statusNote.toLowerCase().contains("draw")) {
                result = Result.DRAW;
            } else {
                throw new RuntimeException("unexpected result type");
            }
        } else {
            throw new RuntimeException("unexpected result type");
        }

        MatchResult matchResult = new MatchResult(
                matchID,
                DataSource.CRICAPI,
                0, // winner TeamID to be set after matching with DB
                0, // toss winner TeamID to be set later
                tossDecision,
                marginSize,
                marginType,
                result,
                winningTeamKey
        );

        // get match
        CricketMatch match = new CricketMatch(
                matchID,
                DataSource.CRICAPI,
                matchDate,
                0, // venueID to be filled by DB lookup
                format,
                venueKey
        );

        // get match teams
        List<MatchTeam> matchTeams = new ArrayList<>();
        matchTeams.add(new MatchTeam(0, matchID, DataSource.CRICAPI, 0, new TeamKey(teams.get(0).getName())));
        matchTeams.add(new MatchTeam(0, matchID, DataSource.CRICAPI, 0, new TeamKey(teams.get(1).getName())));

        // build schema
        return new CricketMatchDataSchema(
                null,
                teams,
                List.of(new Venue(0, venueKey)),
                null,
                List.of(matchResult),
                List.of(match),
                matchTeams
        );
    }
}