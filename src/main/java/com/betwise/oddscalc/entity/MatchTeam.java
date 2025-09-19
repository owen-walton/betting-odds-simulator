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

    public MatchTeam() {

    }

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
