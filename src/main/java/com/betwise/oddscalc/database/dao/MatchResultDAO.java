package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.MatchResult;

import java.sql.PreparedStatement;

public class MatchResultDAO implements WriteDAO<MatchResult>, AutoCloseable {

    private DBConnection dbConnection;

    // initialise connection
    public MatchResultDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean insert(MatchResult matchResult) {
        String sql = "INSERT INTO MatchResult " +
                "(MatchID, WinningTeamID, TossWinningTeamID, TossDecision, Result, MarginSize, MarginType)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setInt(1, matchResult.matchID());

            if (matchResult.winningTeamID() != null) {
                statement.setInt(2, matchResult.winningTeamID());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }

            statement.setInt(3, matchResult.tossWinningTeamID());
            statement.setString(4, matchResult.tossDecision().toString());

            if (matchResult.result() != null) {
                statement.setString(5, matchResult.result().toString());
            } else {
                statement.setNull(5, java.sql.Types.VARCHAR);
            }

            if (matchResult.marginSize() != null) {
                statement.setInt(6, matchResult.marginSize());
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }

            if (matchResult.marginType() != null) {
                statement.setString(7, matchResult.marginType().toString());
            } else {
                statement.setNull(7, java.sql.Types.VARCHAR);
            }

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