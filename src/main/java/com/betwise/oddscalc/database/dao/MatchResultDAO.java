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
            statement.setInt(1, matchResult.getMatchID());

            if (matchResult.getWinningTeamID() != null) {
                statement.setInt(2, matchResult.getWinningTeamID());
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }

            statement.setInt(3, matchResult.getTossWinningTeamID());
            statement.setString(4, matchResult.getTossDecision().toString());

            if (matchResult.getResult() != null) {
                statement.setString(5, matchResult.getResult().toString());
            } else {
                statement.setNull(5, java.sql.Types.VARCHAR);
            }

            if (matchResult.getMarginSize() != null) {
                statement.setInt(6, matchResult.getMarginSize());
            } else {
                statement.setNull(6, java.sql.Types.INTEGER);
            }

            if (matchResult.getMarginType() != null) {
                statement.setString(7, matchResult.getMarginType().toString());
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