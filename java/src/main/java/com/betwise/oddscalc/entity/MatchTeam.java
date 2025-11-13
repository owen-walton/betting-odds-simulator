/**
 * @author Owen Walton
 * Container class that mirrors a singular record in the MatchTeam database table
 * ,
 * Additionally to the DB fields, object stores a TeamKey Java Record for when the teamID the foreign key refers to
 * has not yet been inserted to DB so has no primary key, meaning to keep the foreign reference,
 * natural key, (teamName) must be stored, (each team has a unique teamName
 * this could have been as a string but for readability and to be future change-friendly, TeamKey object is used
 */

package com.betwise.oddscalc.entity;

public class MatchTeam {
    private int matchTeamID;
    private String matchID;
    private DataSource dataSource;
    private int teamID;
    private TeamKey teamNaturalKey;

    public MatchTeam(int matchTeamID, String matchID, DataSource dataSource, int teamID) {
        this.matchTeamID = matchTeamID;
        this.matchID = matchID;
        this.dataSource = dataSource;
        this.teamID = teamID;
    }

    public MatchTeam(int matchTeamID, String matchID, DataSource dataSource, int teamID, TeamKey teamNaturalKey) {
        this.matchTeamID = matchTeamID;
        this.matchID = matchID;
        this.dataSource = dataSource;
        this.teamID = teamID;
        this.teamNaturalKey = teamNaturalKey;
    }

    //============================================================================
    // Getters and Setters
    //============================================================================
    public int getMatchTeamID() {
        return matchTeamID;
    }

    public void setMatchTeamID(int matchTeamID) {
        this.matchTeamID = matchTeamID;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public TeamKey getTeamNaturalKey() {
        return teamNaturalKey;
    }

    public void setTeamNaturalKey(TeamKey teamNaturalKey) {
        this.teamNaturalKey = teamNaturalKey;
    }
}
