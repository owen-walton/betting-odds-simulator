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

    private Set<String> countries;

    public CricAPIClient(HTTPClient httpClient, Set<String> countries) {
        this.httpClient = httpClient;
        this.apiKey = extractApiKey();
        this.countries = countries;
    }

    public String extractApiKey() {
        Properties cricapiProps = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("cricapi/cricapi.properties")) {

            if (input == null) {
                throw new RuntimeException("Can't find cricapi/cricapi.properties in resources");
            }

            cricapiProps.load(input);
            return cricapiProps.getProperty("apikey3");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // not enough data from initial get, matchID is field of interest
    public String getMatchListJson(int offset) throws IOException {
        String url = String.format("%s/matches?apikey=%s&offset=%d", BASE_URL, apiKey, offset);
        return httpClient.get(url);
    }

    @SuppressWarnings("unchecked")
    public Set<String> loadInternationalCountries() throws IOException {
        Set<String> countries = new HashSet<>();
        int offset = 0;

        while (true) {
            // page through /countries by offset
            String url = String.format(
                    "%s/countries?apikey=%s&offset=%d",
                    BASE_URL, apiKey, offset
            );
            String json = httpClient.get(url);
            Map<String, Object> root = ParseJSON.parseJsonToMap(json);
            List<Map<String, Object>> data =
                    (List<Map<String, Object>>) root.get("data");

            if (data == null || data.isEmpty()) {
                // no more pages
                break;
            }

            for (Map<String, Object> c : data) {
                String name = Objects.toString(c.get("name"), "").trim();
                name = Normaliser.normalise(name);
                if (!name.isEmpty()) {
                    countries.add(name);
                }
            }

            // advance offset by number of records returned
            offset += data.size();
        }

        return countries;
    }

    public Set<String> getCountries() {
        return countries;
    }

    public void setCountries(Set<String> countries) {
        this.countries = countries;
    }

    // Returns every matchID whose status == "Completed",
    // both teams are in the international country list,
    // and neither team name contains "Women".
    // match is not before fromDate
    /*@SuppressWarnings("unchecked")
    public Set<String> getCompletedMatchIds(LocalDate fromDate) throws IOException {
        Set<String> ids = new HashSet<>();
        Set<String> countries = loadInternationalCountries();
        int offset = 0;

        while (true) {
            String json = getMatchListJson(offset);
            Map<String, Object> root = ParseJSON.parseJsonToMap(json);
            List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("data");
            if (data == null || data.isEmpty()) break;

            for (Map<String, Object> m : data) {
                String dateStr = Objects.toString(m.get("dateTimeGMT"), Objects.toString(m.get("date")));
                if (dateStr.length() < 10) {
                    continue;  // no valid date
                }
                LocalDate matchDate = LocalDate.parse(dateStr.substring(0, 10));
                if (matchDate.isBefore(fromDate)) {
                    continue;
                }

                String status = Objects.toString(m.get("status"), "");
                if (!"Completed".equalsIgnoreCase(status)) continue;

                String team1 = Objects.toString(m.get("team-1"), "");
                String team2 = Objects.toString(m.get("team-2"), "");

                // filter out any "Women" team or non-international side
                if (team1.toLowerCase().contains("women")
                        || team2.toLowerCase().contains("women")
                        || !countries.contains(team1)
                        || !countries.contains(team2)) {
                    continue;
                }
                ids.add((String) m.get("id"));
            }
            offset += data.size();
        }
        return ids;
    }*/


    // get “Last 7 Days matches + Next 7 Days + Live” from cricScore
    // then returns only those played in the past 7 days.
    @SuppressWarnings("unchecked")
    public Set<String> getMatchIDsWithinLast7DaysFrom(LocalDate fromDateIncl) throws IOException {
        // hit the eCricScore API
        String url = String.format("%s/cricScore?apikey=%s", BASE_URL, apiKey);
        String json = httpClient.get(url);

        System.out.println(json);
        Map<String, Object> root = ParseJSON.parseJsonToMap(json);
        List<Map<String, Object>> matches = (List<Map<String, Object>>) root.get("data");
        if (matches == null) {
            throw new RuntimeException("No data returned from eCricScore");
        }

        // filter to completed matches in the last 7 days
        LocalDate today = LocalDate.now();
        Set<String> ids = new HashSet<>();

        for (Map<String, Object> m : matches) {
            // eCricScore returns a text score only for completed games
            String matchStatus = Objects.toString(m.get("ms"));
            if (!matchStatus.equalsIgnoreCase("result")) {
                continue;  // upcoming fixture or live game
            }

            // parse date
            String dateStr = Objects.toString(m.get("dateTimeGMT"), Objects.toString(m.get("date")));
            if (dateStr.length() < 10) {
                continue;  // no valid date
            }
            LocalDate matchDate = LocalDate.parse(dateStr.substring(0, 10));
            if (matchDate.isBefore(fromDateIncl) || matchDate.isAfter(today)) {
                continue;  // date is before from date (or a future match the result check didn't catch)
            }

            // convert 'England [ENG]' to 'England' etc
            String t1 = Objects.toString(m.get("t1"), "");
            String t2 = Objects.toString(m.get("t2"), "");
            String team1name = Normaliser.normalise(t1.split("\\[")[0]);
            String team2name = Normaliser.normalise(t2.split("\\[")[0]);

            System.out.println(t1 + " vs " + t2);
            System.out.println("'" + team1name + "' & '" + team2name + "'");
            // filter out women’s or non‐international games
            if (team1name.toLowerCase().contains("women")
                    || team2name.toLowerCase().contains("women")
                    || !getCountries().contains(team1name)
                    || !getCountries().contains(team2name)) {
                System.out.println("true");
                continue;
            }
            System.out.println("false");

            // collect the match ID
            String id = Objects.toString(m.get("id"), "");
            if (!id.isBlank()) {
                ids.add(id);
            }
        }
        return ids;
    }

    // fetches the detailed match info by matchID
    public String getMatchDetailsJson(String matchID) throws IOException {
        String url = String.format("%s/match_info?apikey=%s&id=%s", BASE_URL, apiKey, matchID);
        return httpClient.get(url);
    }

    public CricketMatchDataSchema parseAllMatchesWithin7DaysSince(LocalDate fromDate) throws IOException {
        // add all matchIDs of finished matches after fromDate
        Set<String> idSet = getMatchIDsWithinLast7DaysFrom(fromDate);

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
        System.out.println(matchJson);
        // null check
        if (matchJson == null) {
            throw new RuntimeException("Error getting match details");
        }

        // get map of match details
        Map<String, Object> root = ParseJSON.parseJsonToMap(matchJson);
        Map<String, Object> matchInfoMap = (Map<String, Object>) root.get("data");

        // double check match has correct date
        String dateStr = (String) matchInfoMap.get("date");
        // dateStr already assigned earlier in method for a safety check
        String dtGmt = (String) matchInfoMap.get("dateTimeGMT");
        if (dtGmt != null && dtGmt.length() >= 10) {
            dateStr = dtGmt.substring(0, 10);
        }
        if (dateStr == null) {
            throw new RuntimeException("Missing status or date in json");
        }
        LocalDate matchDate = LocalDate.parse(dateStr);
        if (matchDate.isBefore(fromDate)) {
            return new CricketMatchDataSchema(); // skip match
        }

        // get match format
        String format = (String) matchInfoMap.get("matchType");
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
        String homeTeamName = ((List<String>) matchInfoMap.get("teams")).get(0);
        String awayTeamName = ((List<String>) matchInfoMap.get("teams")).get(1);
        teams.add(new Team(0, homeTeamName));
        teams.add(new Team(0, awayTeamName));
        if (homeTeamName == null || awayTeamName == null) {
            return new CricketMatchDataSchema();
        }

        // get venue
        String venueStr = (String) matchInfoMap.get("venue");
        String ground = venueStr.split(",", 2)[0];
        String city = venueStr.split(",", 2)[1];
        VenueKey venueKey = Normaliser.normaliseVenueKey(new VenueKey(ground, city));

        // get result
        TeamKey tossWinningTeamKey = new TeamKey((String) matchInfoMap.get("tossWinner"));
        String toss = (String) matchInfoMap.get("tossChoice");
        TossDecision tossDecision =
                switch (toss == null ? "" : toss.toLowerCase()) {
                    case "bat" -> TossDecision.BAT;
                    case "bowl" -> TossDecision.FIELD;
                    default -> {
                        throw new RuntimeException("Invalid toss decision");
                    }
                };

        String statusNote = (String) matchInfoMap.get("status");
        Result result = null;
        TeamKey winningTeamKey = null;
        Integer marginSize = null;
        MarginType marginType = null;
        String winner = null;
        if (statusNote.contains(homeTeamName)) {
            winningTeamKey = new TeamKey(homeTeamName);
            result = Result.WIN;
        } else if (statusNote.contains(awayTeamName)) {
            winningTeamKey = new TeamKey(awayTeamName);
            result = Result.WIN;
        }

        String note = statusNote.toLowerCase();
        if (note.contains("wicket")) {
            try {
                marginSize = Integer.parseInt(statusNote.replaceAll("[^0-9]", ""));
                marginType = MarginType.WICKETS;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (note.contains("run")) {
            try {
                marginSize = Integer.parseInt(statusNote.replaceAll("[^0-9]", ""));
                marginType = MarginType.RUNS;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (statusNote.toLowerCase().contains("tie")) {
            result = Result.TIE;
        } else if (statusNote.

                toLowerCase().

                contains("no result")) {
            result = Result.NO_RESULT;
        } else if (statusNote.toLowerCase().contains("draw")) {
            result = Result.DRAW;
        } // fallback for DLS or unknown win margins (if win but wicket or run not found)
        else if (result == Result.WIN) {
            marginType = MarginType.UNKNOWN;
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
                winningTeamKey,
                tossWinningTeamKey
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
        matchTeams.

                add(new

                        MatchTeam(0, matchID, DataSource.CRICAPI, 0, new TeamKey(teams.get(0).

                        getName())));
        matchTeams.

                add(new

                        MatchTeam(0, matchID, DataSource.CRICAPI, 0, new TeamKey(teams.get(1).

                        getName())));

        // build schema
        return new

                CricketMatchDataSchema(
                null,
                teams,
                List.of(new Venue(0, venueKey)),
                null,
                List.

                        of(matchResult),
                List.

                        of(match),

                matchTeams
        );
    }
}