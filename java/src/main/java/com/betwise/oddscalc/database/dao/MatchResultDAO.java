/**
 * @author Owen Walton
 * Data Access Object for MatchResult table in database
 */

package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.MarginType;
import com.betwise.oddscalc.entity.MatchResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

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
                "(MatchID, DataSource, WinningTeamID, TossWinningTeamID, TossDecision, Result, MarginSize, MarginType)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, matchResult.getMatchID());

            statement.setString(2, matchResult.getDataSource().name());

            if (matchResult.getWinningTeamID() != null) {
                statement.setInt(3, matchResult.getWinningTeamID());
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }

            statement.setInt(4, matchResult.getTossWinningTeamID());
            statement.setString(5, matchResult.getTossDecision().toString());

            if (matchResult.getResult() != null) {
                statement.setString(6, matchResult.getResult().toString());
            } else {
                statement.setNull(6, java.sql.Types.VARCHAR);
            }

            if (matchResult.getMarginSize() != null) {
                statement.setInt(7, matchResult.getMarginSize());
            } else {
                statement.setNull(7, java.sql.Types.INTEGER);
            }

            if (matchResult.getMarginType() != null) {
                statement.setString(8, matchResult.getMarginType().toString());
            } else {
                statement.setNull(8, java.sql.Types.VARCHAR);
            }

            statement.executeUpdate();
            dbConnection.getConn().commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // insert a List of MatchResult objects to DB
    // adds each singular object to the same batch before executing the entire batch to reduce querying
    // this increases efficiency and reduces points of failure
    @Override
    public void bulkInsertIfNotExists(List<MatchResult> matchResults) {
        if (matchResults.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO MatchResult " +
                "(MatchID, DataSource, WinningTeamID, TossWinningTeamID, TossDecision, Result, MarginSize, MarginType) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "WinningTeamID = VALUES(WinningTeamID), " +
                "TossWinningTeamID = VALUES(TossWinningTeamID), " +
                "TossDecision = VALUES(TossDecision), " +
                "Result = VALUES(Result), " +
                "MarginSize = VALUES(MarginSize), " +
                "MarginType = VALUES(MarginType)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            // build bulk insert
            for (MatchResult matchResult : matchResults) {
                statement.setString(1, matchResult.getMatchID());

                statement.setString(2, matchResult.getDataSource().name());

                if (matchResult.getWinningTeamID() != null && matchResult.getWinningTeamID() != -1) {
                    statement.setInt(3, matchResult.getWinningTeamID());
                } else {
                    statement.setNull(3, java.sql.Types.INTEGER);
                }

                statement.setInt(4, matchResult.getTossWinningTeamID());
                statement.setString(5, matchResult.getTossDecision().toString());

                if (matchResult.getResult() != null) {
                    statement.setString(6, matchResult.getResult().toString());
                } else {
                    statement.setNull(6, java.sql.Types.VARCHAR);
                }

                if (matchResult.getMarginSize() != null) {
                    statement.setInt(7, matchResult.getMarginSize());
                } else {
                    statement.setNull(7, java.sql.Types.INTEGER);
                }

                if (matchResult.getMarginType() != null) {
                    statement.setString(8, matchResult.getMarginType().toString());
                } else {
                    statement.setString(8, MarginType.UNKNOWN.toString());
                }
                statement.addBatch();
            }

            // executed bulk insert
            statement.executeBatch();
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}