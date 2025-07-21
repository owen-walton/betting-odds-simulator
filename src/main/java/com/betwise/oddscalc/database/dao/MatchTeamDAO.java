package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.MatchTeam;

import java.sql.PreparedStatement;

public class MatchTeamDAO implements WriteDAO<MatchTeam>, AutoCloseable {

    private DBConnection dbConnection;

    // initialise connection
    public MatchTeamDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean insert(MatchTeam matchTeam) {
        String sql = "INSERT INTO MatchTeam " +
                "(MatchID, TeamID) " +
                "VALUES (?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setInt(1, matchTeam.matchID());
            statement.setInt(2, matchTeam.teamID());

            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}
