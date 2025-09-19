package com.betwise.oddscalc.entity;

public class Team {
    private int teamID;
    private String name;
    private Float elo; // can be null

    public Team() {
        this.teamID = -1;
        this.name = null;
        this.elo = null;
    }

    public Team(int teamID, String name) {
        this.teamID = teamID;
        this.name = name;
        this.elo = null;
    }

    public Team(int teamID, String name, float elo) {
        this.teamID = teamID;
        this.name = name;
        this.elo = elo;
    }

    public Team(int teamID, String name, double elo) {
        this.teamID = teamID;
        this.name = name;
        this.elo = (float) elo;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getElo() {
        return elo;
    }

    public void setElo(Float elo) {
        this.elo = elo;
    }
}
