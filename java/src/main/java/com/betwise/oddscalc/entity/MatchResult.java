/**
 * @author Owen Walton
 * Container class that mirrors a singular record in the MatchResult database table
 * ,
 * Additionally to the DB fields, object stores 2 TeamKey Java Records for when the teamID the foreign key refers to
 * has not yet been inserted to DB so has no primary key, meaning to keep the foreign reference,
 * natural key, (teamName) must be stored,
 * this could have been as a string but for readability and to be future change-friendly, TeamKey object is used
 */

package com.betwise.oddscalc.entity;

public class MatchResult {

    private String matchID;
    private DataSource dataSource;
    private Integer winningTeamID; // nullable for draw/no result
    private int tossWinningTeamID;
    private TossDecision tossDecision;
    private Integer marginSize; // nullable for draw/no result
    private MarginType marginType;
    private Result result;
    private TeamKey winningTeamNaturalKey;
    private TeamKey tossWinningTeamNaturalKey;

    public MatchResult(String matchID, DataSource dataSource, Integer winningTeamID, int tossWinningTeamID, TossDecision tossDecision, Integer marginSize, MarginType marginType, Result result) {
        this.matchID = matchID;
        this.dataSource = dataSource;
        this.winningTeamID = winningTeamID;
        this.tossWinningTeamID = tossWinningTeamID;
        this.tossDecision = tossDecision;
        this.marginSize = marginSize;
        this.marginType = marginType;
        this.result = result;
    }

    public MatchResult(String matchID, DataSource dataSource, Integer winningTeamID, int tossWinningTeamID, TossDecision tossDecision, Integer marginSize, MarginType marginType, Result result, TeamKey winningTeamNaturalKey, TeamKey tossWinningTeamNaturalKey) {
        this.matchID = matchID;
        this.dataSource = dataSource;
        this.winningTeamID = winningTeamID;
        this.tossWinningTeamID = tossWinningTeamID;
        this.tossDecision = tossDecision;
        this.marginSize = marginSize;
        this.marginType = marginType;
        this.result = result;
        this.winningTeamNaturalKey = winningTeamNaturalKey;
        this.tossWinningTeamNaturalKey = tossWinningTeamNaturalKey;
    }

    //============================================================================
    // Getters and Setters
    //============================================================================
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

    public TeamKey getTossWinningTeamNaturalKey() {
        return tossWinningTeamNaturalKey;
    }

    public void setTossWinningTeamNaturalKey(TeamKey tossWinningTeamNaturalKey) {
        this.tossWinningTeamNaturalKey = tossWinningTeamNaturalKey;
    }

    //============================================================================
    // toString
    //============================================================================
    @Override
    public String toString() {
        return "MatchResult{" +
                "matchID='" + matchID + '\'' +
                ", dataSource=" + (dataSource != null ? dataSource.name() : "null") +
                ", winningTeamID=" + (winningTeamID != null ? winningTeamID : "null") +
                ", tossWinningTeamID=" + tossWinningTeamID +
                ", tossDecision=" + (tossDecision != null ? tossDecision.name() : "null") +
                ", marginSize=" + (marginSize != null ? marginSize : "null") +
                ", marginType=" + (marginType != null ? marginType.name() : "null") +
                ", result=" + (result != null ? result.name() : "null") +
                ", winningTeamNaturalKey=" + (winningTeamNaturalKey != null ? winningTeamNaturalKey.toString() : "null") +
                ", tossWinningTeamNaturalKey=" + (tossWinningTeamNaturalKey != null ? tossWinningTeamNaturalKey.toString() : "null") +
                '}';
    }
}
