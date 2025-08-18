package com.betwise.oddscalc.ingestdata;

import com.betwise.oddscalc.entity.CricketMatchDataSchema;
import com.betwise.oddscalc.entity.Team;
import com.betwise.oddscalc.entity.TeamHomeVenue;
import com.betwise.oddscalc.entity.VenueKey;
import com.betwise.oddscalc.ingestdata.ingestutils.HTTPClient;
import com.betwise.oddscalc.ingestdata.ingestutils.ParseJSON;
import com.betwise.oddscalc.ingestdata.ingestutils.VenueNormaliser;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

public class CricAPIClient {
    private final HTTPClient httpClient;
    private final String apiKey;
    private final String BASE_URL = "https://api.cricketdata.org/v1";

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
    public String getMatchListJson(LocalDate fromDate, int page) throws IOException {
        String dateString = fromDate.getYear() + "-" +
                String.format("%02d", fromDate.getMonthValue()) + "-" +
                String.format("%02d", fromDate.getDayOfMonth());
        String url = String.format(
                "%s/matches?apikey=%s&date=%s&international=true&page=%d&gender=male",
                BASE_URL,
                apiKey,
                dateString,
                page
        );
        return httpClient.get(url);
    }

    // fetches the detailed match info by matchID
    public String getMatchDetailsJson(String matchID) throws IOException {
        String url = String.format(
                "%s/matches/%s?apikey=%s",
                BASE_URL,
                matchID,
                apiKey
        );
        return httpClient.get(url);
    }

    @SuppressWarnings("unchecked")
    public CricketMatchDataSchema parseAllMatchesSince(LocalDate fromDate) throws IOException {
        // add all matchIDs of finished matches after fromDate
        Set<String> idSet = new HashSet<>();
        int page = 1;
        int totalPages;
        do {
            Map<String, Object> jsonMap = ParseJSON.parseJsonToMap(getMatchListJson(fromDate, page));
            List<Map<String, Object>> matches = (List<Map<String, Object>>) ParseJSON.getValueFromMap("matches",
                    jsonMap);

            if (matches == null) {
                throw new RuntimeException("No matches available since the date given");
            }

            for (Map<String, Object> match : matches) {
                String status    = (String) match.get("status");
                String startDate = (String) match.get("startDate");
                if (status.equalsIgnoreCase("completed")
                        && !LocalDate.parse(startDate).isBefore(fromDate)) {
                    idSet.add((String) match.get("id"));
                }

            }
            // calculate total pages
            Map<String,Object> pagination =
                    (Map<String,Object>) jsonMap.get("pagination");
            totalPages = ((Integer) pagination.get("totalPages"));

            page++;
        } while (page <= totalPages);

        // use matchID set to parse all matches
        CricketMatchDataSchema schema = new CricketMatchDataSchema();
        for (String matchID : idSet) {
            schema.appendSchema(parseSingleMatch(matchID));
        }
        return schema;
    }

    public CricketMatchDataSchema parseSingleMatch(String matchID) throws IOException {

        // get map of match details
        Map<String, Object> matchInfoMap = ParseJSON.parseJsonToMap(getMatchDetailsJson(matchID));

        // get match format
        String format = (String) ParseJSON.getValueFromMap("match/format", matchInfoMap);
        if (format == null) {
            return new CricketMatchDataSchema();
        }
        if (format.equalsIgnoreCase("T20I")) {
            format = "T20";
        }
        if (format.equalsIgnoreCase("4-Day Test")) {
            format = "Test";
        }
        if (!format.equalsIgnoreCase("Test") &&
                        !format.equalsIgnoreCase("T20") &&
                        !format.equalsIgnoreCase("ODI")) {
            return new CricketMatchDataSchema();
        }

        // get teams
        List<Team> teams = new ArrayList<>();
        String homeTeamName = ((String)ParseJSON.getValueFromMap("match/teams/home", matchInfoMap)).split(" ")[0];
        String awayTeamName = ((String)ParseJSON.getValueFromMap("match/teams/away", matchInfoMap)).split(" ")[0];
        teams.add(new Team(0, homeTeamName));
        teams.add(new Team(0, awayTeamName));

        // get venues
        String ground = (String)ParseJSON.getValueFromMap("match/venue/ground", matchInfoMap);
        String city = (String)ParseJSON.getValueFromMap("match/venue/city", matchInfoMap);
        VenueKey venueKey = VenueNormaliser.normaliseVenueKey(new VenueKey(ground, city));

        // TODO get team home venues
        // TeamHomeVenue teamHomeVenue = new TeamHomeVenue(0, )

        // add team home venue
        //CricketMatchDataSchema tempSchema;

    }
}