package com.betwise.oddscalc.entity;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class Match {

    private MatchFormat format;
    private Date startDate;
    private int matchLengthInDays;
    private Venue venue;
    private Map<Team, HomeStatus> teams;
    private Team tossWinner;

    public Match() {
    }

    public Match(MatchFormat format, Date startDate, int matchLengthInDays,
                 Venue venue, Map<Team, HomeStatus> teams, Team tossWinner) {
        this.format = format;
        this.startDate = startDate;
        this.matchLengthInDays = matchLengthInDays;
        this.venue = venue;
        this.teams = teams;
        this.tossWinner = tossWinner;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public void setFormat(MatchFormat format) {
        this.format = format;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getMatchLengthInDays() {
        return matchLengthInDays;
    }

    public void setMatchLengthInDays(int matchLengthInDays) {
        this.matchLengthInDays = matchLengthInDays;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Map<Team, HomeStatus> getTeams() {
        return teams;
    }

    public void setTeams(Map<Team, HomeStatus> teams) {
        this.teams = teams;
    }

    public Team getTossWinner() {
        return tossWinner;
    }

    public void setTossWinner(Team tossWinner) {
        this.tossWinner = tossWinner;
    }

}
