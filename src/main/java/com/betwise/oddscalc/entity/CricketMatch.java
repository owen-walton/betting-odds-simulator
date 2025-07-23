package com.betwise.oddscalc.entity;

import java.time.LocalDate;

public class CricketMatch {
    private int matchID;
    private LocalDate startDate;
    private DataSource dataSource;
    private String formatName;
    private int venueID;
    private VenueKey venueNaturalKey;

    public CricketMatch(int matchID, DataSource dataSource, LocalDate startDate, int venueID, String formatName, VenueKey venueNaturalKey) {
        this.matchID = matchID;
        this.dataSource = dataSource;
        this.startDate = startDate;
        this.venueID = venueID;
        this.formatName = formatName;
        this.venueNaturalKey = venueNaturalKey;
    }

    public int getMatchID() {
        return matchID;
    }

    public void setMatchID(int matchID) {
        this.matchID = matchID;
    }

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getVenueID() {
        return venueID;
    }

    public void setVenueID(int venueID) {
        this.venueID = venueID;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public VenueKey getVenueNaturalKey() {
        return venueNaturalKey;
    }

    public void setVenueNaturalKey(VenueKey venueNaturalKey) {
        this.venueNaturalKey = venueNaturalKey;
    }
}
