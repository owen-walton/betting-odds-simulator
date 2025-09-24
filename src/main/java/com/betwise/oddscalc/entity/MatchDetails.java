package com.betwise.oddscalc.entity;

import java.time.LocalDate;
import java.util.Map;

public class MatchDetails {

    private String matchID;
    private String dataSource;
    private LocalDate startDate;

    private Map<Integer, Boolean> teamHomeMap;
    private Map<Integer, Double> teamEloMap;

    private Integer winningTeamID;
    private int tossWinningTeamID;
    private String tossDecision;
    private String result;
    private int marginSize;
    private String marginType;

    public MatchDetails() {}

    public String getMatchID() { return matchID; }
    public void setMatchID(String matchID) { this.matchID = matchID; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public Map<Integer, Boolean> getTeamHomeMap() { return teamHomeMap; }
    public void setTeamHomeMap(Map<Integer, Boolean> teamHomeMap) { this.teamHomeMap = teamHomeMap; }

    public Map<Integer, Double> getTeamEloMap() { return teamEloMap; }
    public void setTeamEloMap(Map<Integer, Double> teamEloMap) { this.teamEloMap = teamEloMap; }

    public Integer getWinningTeamID() { return winningTeamID; }
    public void setWinningTeamID(Integer winningTeamID) { this.winningTeamID = winningTeamID; }

    public int getTossWinningTeamID() { return tossWinningTeamID; }
    public void setTossWinningTeamID(int tossWinningTeamID) { this.tossWinningTeamID = tossWinningTeamID; }

    public String getTossDecision() { return tossDecision; }
    public void setTossDecision(String tossDecision) { this.tossDecision = tossDecision; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public int getMarginSize() { return marginSize; }
    public void setMarginSize(int marginSize) { this.marginSize = marginSize; }

    public String getMarginType() { return marginType; }
    public void setMarginType(String marginType) { this.marginType = marginType; }
}
