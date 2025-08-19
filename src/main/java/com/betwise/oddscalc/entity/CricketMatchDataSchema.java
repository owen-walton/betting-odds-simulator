package com.betwise.oddscalc.entity;

import java.util.ArrayList;
import java.util.List;

public class CricketMatchDataSchema {
    private List<MatchFormat> matchFormats;
    private List<Team> teams;
    private List<Venue> venues;
    private List<TeamHomeVenue> teamHomeVenues;
    private List<CricketMatch> cricketMatches;
    private List<MatchResult> matchResults;
    private List<MatchTeam> matchTeams;

    public CricketMatchDataSchema() {
        this.matchFormats = null; // entered into db from DDL and should only be assigned when derived from db
        this.teams = new ArrayList<>();
        this.venues = new ArrayList<>();
        this.teamHomeVenues = new ArrayList<>();
        this.matchResults = new ArrayList<>();
        this.cricketMatches = new ArrayList<>();
        this.matchTeams = new ArrayList<>();
    }

    public CricketMatchDataSchema(
            List<MatchFormat> matchFormats,
            List<Team> teams,
            List<Venue> venues,
            List<TeamHomeVenue> teamHomeVenues,
            List<MatchResult> matchResults,
            List<CricketMatch> cricketMatches,
            List<MatchTeam> matchTeams
    ) {
        this.matchFormats = matchFormats;
        this.teams = teams;
        this.venues = venues;
        this.teamHomeVenues = teamHomeVenues;
        this.matchResults = matchResults;
        this.cricketMatches = cricketMatches;
        this.matchTeams = matchTeams;
    }

    // getters and setters
    public List<MatchFormat> getMatchFormats() {
        return matchFormats;
    }

    public void setMatchFormats(List<MatchFormat> matchFormats) {
        this.matchFormats = matchFormats;
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public void setVenues(List<Venue> venues) {
        this.venues = venues;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<TeamHomeVenue> getTeamHomeVenues() {
        return teamHomeVenues;
    }

    public void setTeamHomeVenues(List<TeamHomeVenue> teamHomeVenues) {
        this.teamHomeVenues = teamHomeVenues;
    }

    public List<CricketMatch> getCricketMatches() {
        return cricketMatches;
    }

    public void setCricketMatches(List<CricketMatch> cricketMatches) {
        this.cricketMatches = cricketMatches;
    }

    public List<MatchResult> getMatchResults() {
        return matchResults;
    }

    public void setMatchResults(List<MatchResult> matchResults) {
        this.matchResults = matchResults;
    }

    public List<MatchTeam> getMatchTeams() {
        return matchTeams;
    }

    public void setMatchTeams(List<MatchTeam> matchTeams) {
        this.matchTeams = matchTeams;
    }

    // the natural key field of matchTeam must be initialised
    // updates foreign key usage of team object with the id in team
    public void updateTeamKey(Team team) {
        TeamKey key = new TeamKey(team.getName());

        // update match team pojo
        for (MatchTeam matchTeam : getMatchTeams()) {
            if (key.equals(matchTeam.getTeamNaturalKey())) {
                matchTeam.setTeamID(team.getTeamID());
            }
        }

        // update match result pojo
        for (MatchResult matchResult : getMatchResults()) {
            if (key.equals(matchResult.getWinningTeamNaturalKey())) {
                matchResult.setWinningTeamID(team.getTeamID());
            }
            if (key.equals(matchResult.getTossWinningTeamNaturalKey())) {
                matchResult.setTossWinningTeamID(team.getTeamID());
            }
        }
    }

    public void updateVenueKey(Venue venue) {
        VenueKey key = new VenueKey(venue.getGroundName(), venue.getCity());

        // update cricket match pojo
        for (CricketMatch cricketMatch : getCricketMatches()) {
            if (key.equals(cricketMatch.getVenueNaturalKey())) {
                cricketMatch.setVenueID(venue.getVenueID());
            }
        }
    }

    public void appendSchema(CricketMatchDataSchema tempSchema) {
        if(tempSchema.getMatchFormats() != null) {
            this.matchFormats.addAll(tempSchema.getMatchFormats());
        }
        if (tempSchema.getTeams() != null) {
            this.teams.addAll(tempSchema.getTeams());
        }
        if (tempSchema.getVenues() != null) {
            this.venues.addAll(tempSchema.getVenues());
        }
        if (tempSchema.getTeamHomeVenues() != null) {
            this.teamHomeVenues.addAll(tempSchema.getTeamHomeVenues());
        }
        if (tempSchema.getMatchResults() != null) {
            this.matchResults.addAll(tempSchema.getMatchResults());
        }
        if (tempSchema.getCricketMatches() != null) {
            this.cricketMatches.addAll(tempSchema.getCricketMatches());
        }
        if (tempSchema.getMatchTeams() != null) {
            this.matchTeams.addAll(tempSchema.getMatchTeams());
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CricketMatchDataSchema {")
                .append("\n  matchFormats: ").append(matchFormats)
                .append("\n  teams: ").append(teams)
                .append("\n  venues: ").append(venues)
                .append("\n  teamHomeVenues: ").append(teamHomeVenues)
                .append("\n  cricketMatches: ").append(cricketMatches)
                .append("\n  matchResults: ").append(matchResults)
                .append("\n  matchTeams: ").append(matchTeams)
                .append("\n}");
        return sb.toString();
    }
}
