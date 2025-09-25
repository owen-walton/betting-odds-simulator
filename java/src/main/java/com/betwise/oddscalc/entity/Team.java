package com.betwise.oddscalc.entity;

public class Team {
    private int teamID;
    private String name;

    public Team() {
        this.teamID = -1;
        this.name = null;
    }

    public Team(int teamID, String name) {
        this.teamID = teamID;
        this.name = name;
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
}