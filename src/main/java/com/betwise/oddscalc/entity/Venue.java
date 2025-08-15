package com.betwise.oddscalc.entity;

public class Venue {
    private int venueID;
    private String groundName;
    private String city;

    public Venue() {
        this.venueID = -1;
        this.groundName = null;
        this.city = null;
    }

    public Venue(int venueID, VenueKey venueKey) {
        this.venueID = venueID;
        this.groundName = venueKey.groundName();
        this.city = venueKey.city();
    }

    public Venue(int venueID, String groundName, String city) {
        this.venueID = venueID;
        this.groundName = groundName;
        this.city = city;
    }

    public VenueKey getVenueKey() {
        return new VenueKey(this.groundName, this.city);
    }

    public int getVenueID() {
        return venueID;
    }

    public void setVenueID(int venueID) {
        this.venueID = venueID;
    }

    public String getGroundName() {
        return groundName;
    }

    public void setGroundName(String groundName) {
        this.groundName = (groundName == null ? "" : groundName.trim());
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = (city == null ? "" : city.trim());
    }
}