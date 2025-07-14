package com.betwise.oddscalc.repository;

import com.betwise.oddscalc.entity.Team;

public class TeamRepository {

    public TeamRepository() {
    }

    public boolean insertTeam(Team team) {

        if (!checkExists(team)) {

        }

        return false;
    }

    public boolean checkExists(Team team) {
        return false;
    }
}
