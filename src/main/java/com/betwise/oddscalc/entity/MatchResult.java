package com.betwise.oddscalc.entity;

public class MatchResult {
    private int matchID;
    private DataSource dataSource;
    private Integer winningTeamID; // nullable for draw/no result
    private int tossWinningTeamID;
    private TossDecision tossDecision;
    private Integer marginSize; // nullable for draw/no result
    private MarginType marginType;
    private Result result;
    private TeamKey winningTeamNaturalKey;

    public MatchResult(int matchID, DataSource dataSource, Integer winningTeamID, int tossWinningTeamID, TossDecision tossDecision, Integer marginSize, MarginType marginType, Result result) {
        this.matchID = matchID;
        this.dataSource = dataSource;
        this.winningTeamID = winningTeamID;
        this.tossWinningTeamID = tossWinningTeamID;
        this.tossDecision = tossDecision;
        this.marginSize = marginSize;
        this.marginType = marginType;
        this.result = result;
    }

    public MatchResult(int matchID, DataSource dataSource, Integer winningTeamID, int tossWinningTeamID, TossDecision tossDecision, Integer marginSize, MarginType marginType, Result result, TeamKey winningTeamNaturalKey) {
        this.matchID = matchID;
        this.dataSource = dataSource;
        this.winningTeamID = winningTeamID;
        this.tossWinningTeamID = tossWinningTeamID;
        this.tossDecision = tossDecision;
        this.marginSize = marginSize;
        this.marginType = marginType;
        this.result = result;
        this.winningTeamNaturalKey = winningTeamNaturalKey;
    }

    public int getMatchID() {
        return matchID;
    }

    public void setMatchID(int matchID) {
        this.matchID = matchID;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getWinningTeamID() {
        return winningTeamID;
    }

    public void setWinningTeamID(Integer winningTeamID) {
        this.winningTeamID = winningTeamID;
    }

    public int getTossWinningTeamID() {
        return tossWinningTeamID;
    }

    public void setTossWinningTeamID(int tossWinningTeamID) {
        this.tossWinningTeamID = tossWinningTeamID;
    }

    public TossDecision getTossDecision() {
        return tossDecision;
    }

    public void setTossDecision(TossDecision tossDecision) {
        this.tossDecision = tossDecision;
    }

    public Integer getMarginSize() {
        return marginSize;
    }

    public void setMarginSize(Integer marginSize) {
        this.marginSize = marginSize;
    }

    public MarginType getMarginType() {
        return marginType;
    }

    public void setMarginType(MarginType marginType) {
        this.marginType = marginType;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public TeamKey getWinningTeamNaturalKey() {
        return winningTeamNaturalKey;
    }

    public void setWinningTeamNaturalKey(TeamKey winningTeamNaturalKey) {
        this.winningTeamNaturalKey = winningTeamNaturalKey;
    }
}
