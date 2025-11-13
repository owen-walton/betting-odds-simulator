/**
 * @author Owen Walton
 * Container object that represents the ENTIRE database schema (only the tables that hold match data though)
 * For efficiency when inserting, each table has a list storing its container object records, just like the tables
 * ,
 * This container class requires some smart functionality due to the need for mutibility at times
 */

package com.betwise.oddscalc.entity;

import java.util.ArrayList;
import java.util.List;

public class CricketMatchDataSchema {

    private List<Team> teams;
    private List<Venue> venues;
    private List<TeamHomeVenue> teamHomeVenues;
    private List<CricketMatch> cricketMatches;
    private List<MatchResult> matchResults;
    private List<MatchTeam> matchTeams;

    public CricketMatchDataSchema() {
        this.teams = new ArrayList<>();
        this.venues = new ArrayList<>();
        this.teamHomeVenues = new ArrayList<>();
        this.matchResults = new ArrayList<>();
        this.cricketMatches = new ArrayList<>();
        this.matchTeams = new ArrayList<>();
    }

    public CricketMatchDataSchema(
            List<Team> teams,
            List<Venue> venues,
            List<TeamHomeVenue> teamHomeVenues,
            List<MatchResult> matchResults,
            List<CricketMatch> cricketMatches,
            List<MatchTeam> matchTeams
    ) {
        this.teams = teams;
        this.venues = venues;
        this.teamHomeVenues = teamHomeVenues;
        this.matchResults = matchResults;
        this.cricketMatches = cricketMatches;
        this.matchTeams = matchTeams;
    }

    //============================================================================
    // Getters and Setters
    //============================================================================
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

    //============================================================================
    // Smart methods
    //============================================================================

    // takes in a Team object with the correct id,
    // anywhere that the TeamKey in the passed-in object is found within the schema,
    // update the teamID to match the new correct one
    public void updateTeamKey(Team team) {
        String teamName = team.getName().trim().toLowerCase();

        for (MatchTeam matchTeam : getMatchTeams()) {
            if (matchTeam.getTeamNaturalKey().name().trim().toLowerCase().equals(teamName)) {
                matchTeam.setTeamID(team.getTeamID());
            }
        }

        for (MatchResult matchResult : getMatchResults()) {
            if (matchResult.getWinningTeamNaturalKey() != null && matchResult.getWinningTeamNaturalKey().name().trim().toLowerCase().equals(teamName)) {
                matchResult.setWinningTeamID(team.getTeamID());
            }
            if (matchResult.getTossWinningTeamNaturalKey().name().trim().toLowerCase().equals(teamName)) {
                matchResult.setTossWinningTeamID(team.getTeamID());
            }
        }
    }

    // sometimes a more canonical name for an existing Team is come across,
    // then this function is called to replace that team's name
    public void replaceTeamNameEverywhere(String newName, String oldName) {
        // safety check
        if (newName == null || oldName == null || newName.isEmpty() || oldName.isEmpty()) {
            return;
        }

        String oldNameNorm = oldName.trim().toLowerCase();
        String newNameNorm = newName.trim();

        // Update the Team object
        for (Team team : getTeams()) {
            if (team.getName().trim().toLowerCase().equals(oldNameNorm)) {
                team.setName(newNameNorm);
            }
        }

        // Update all MatchTeam references
        for (MatchTeam matchTeam : getMatchTeams()) {
            TeamKey tk = matchTeam.getTeamNaturalKey();
            if (tk.name().trim().toLowerCase().equals(oldNameNorm)) {
                // Replace with new TeamKey keeping the rest of the key intact
                matchTeam.setTeamNaturalKey(new TeamKey(newNameNorm));
            }
        }

        // Update all MatchResult references
        for (MatchResult matchResult : getMatchResults()) {
            if (matchResult.getWinningTeamNaturalKey() != null &&
                    matchResult.getWinningTeamNaturalKey().name().trim().toLowerCase().equals(oldNameNorm)) {
                matchResult.setWinningTeamNaturalKey(new TeamKey(newNameNorm));
            }
            if (matchResult.getTossWinningTeamNaturalKey() != null &&
                    matchResult.getTossWinningTeamNaturalKey().name().trim().toLowerCase().equals(oldNameNorm)) {
                matchResult.setTossWinningTeamNaturalKey(new TeamKey(newNameNorm));
            }
        }
    }

    // takes in a Venue object with the correct id,
    // anywhere that the VenueKey in the passed-in object is found within the schema,
    // update the venueID to match the new correct one
    public void updateVenueKey(Venue venue) {
        String ground = venue.getGroundName().trim().toLowerCase();
        String city = venue.getCity().trim().toLowerCase();

        for (CricketMatch cricketMatch : getCricketMatches()) {
            VenueKey vk = cricketMatch.getVenueNaturalKey();
            if (vk.groundName().trim().toLowerCase().equals(ground) &&
                    vk.city().trim().toLowerCase().equals(city)) {
                cricketMatch.setVenueID(venue.getVenueID());
            }
        }
    }

    // takes in a temporary instance of this class and adds all the values in the temp's lists to this class's lists
    public void appendSchema(CricketMatchDataSchema tempSchema) {
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

    //============================================================================
    // toString
    //============================================================================
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CricketMatchDataSchema {")
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
