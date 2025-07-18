package com.betwise.oddscalc.entity;

import java.time.LocalDate;
import java.util.Map;

public class Match {

    private MatchFormat format;
    private LocalDate startDate;
    private int matchLengthInDays;
    private Venue venue;
    private Map<Team, HomeStatus> teams;
    private MatchResult matchResult;

    public Match() {
    }

    public Match(MatchFormat format, LocalDate startDate, int matchLengthInDays,
                 Venue venue, Map<Team, HomeStatus> teams, MatchResult matchResult) {
        this.format = format;
        this.startDate = startDate;
        this.matchLengthInDays = matchLengthInDays;
        this.venue = venue;
        this.teams = teams;
        this.matchResult = matchResult;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public void setFormat(MatchFormat format) {
        this.format = format;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
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

    public MatchResult getMatchResult() {
        return this.matchResult;
    }

    public void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }

}
