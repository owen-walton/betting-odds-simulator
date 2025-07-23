package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.MatchResult;
import com.betwise.oddscalc.entity.MatchTeam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

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
                "(MatchID, DataSource, TeamID) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setInt(1, matchTeam.getMatchID());
            statement.setString(2, matchTeam.getDataSource().name());
            statement.setInt(3, matchTeam.getTeamID());

            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void bulkInsertIfNotExists(List<MatchTeam> matchTeams) {
        if (matchTeams.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO MatchTeam (MatchID, DataSource, TeamID) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE TeamID = VALUES(TeamID)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (MatchTeam matchTeam : matchTeams) {
                statement.setInt(1, matchTeam.getMatchID());
                statement.setString(2, matchTeam.getDataSource().name());
                statement.setInt(3, matchTeam.getTeamID());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}
