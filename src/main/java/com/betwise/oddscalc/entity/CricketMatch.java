package com.betwise.oddscalc.entity;

import java.time.LocalDate;
import java.util.Map;

public class CricketMatch {

    private int matchID;
    private MatchFormat format;
    private int venueID;
    private LocalDate startDate;
    private Map<String, HomeStatus> teams;
    private MatchResult matchResult;

    public CricketMatch() {
    }

    public CricketMatch(MatchFormat format, LocalDate startDate, int matchLengthInDays,
                        Venue venue, Map<String, HomeStatus> teams, MatchResult matchResult) {
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

    public Map<String, HomeStatus> getTeams() {
        return teams;
    }

    public void setTeams(Map<String, HomeStatus> teams) {
        this.teams = teams;
    }

    public MatchResult getMatchResult() {
        return this.matchResult;
    }

    public void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }

}
