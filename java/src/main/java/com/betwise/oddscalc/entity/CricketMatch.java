/**
 * @author Owen Walton
 * Container class that mirrors a singular record in the CricketMatch database table
 * ,
 * Additionally to the DB fields, object stores a VenueKey Java Record for when the Venue it refers to
 * has not yet been inserted to DB so has no primary key, meaning to keep the foreign reference,
 * natural key, (groundName, city) must be stored
 */
package com.betwise.oddscalc.entity;

import java.time.LocalDate;

public class CricketMatch {
    private String matchID; // part of composite PK
    private LocalDate startDate;
    private DataSource dataSource; // CRICSHEET, CRICAPI - part of composite PK
    private String formatName;
    private int venueID;
    private VenueKey venueNaturalKey;

    public CricketMatch(String matchID, DataSource dataSource, LocalDate startDate, int venueID, String formatName, VenueKey venueNaturalKey) {
        this.matchID = matchID;
        this.dataSource = dataSource;
        this.startDate = startDate;
        this.venueID = venueID;
        this.formatName = formatName;
        this.venueNaturalKey = venueNaturalKey;
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

    //============================================================================
    // toString
    //============================================================================
    @Override
    public String toString() {
        return "CricketMatch{" +
                "matchID='" + matchID + '\'' +
                ", startDate=" + startDate +
                ", dataSource=" + (dataSource != null ? dataSource.name() : "null") +
                ", formatName='" + formatName + '\'' +
                ", venueID=" + venueID +
                ", venueNaturalKey=" + (venueNaturalKey != null ? venueNaturalKey.toString() : "null") +
                '}';
    }
}
