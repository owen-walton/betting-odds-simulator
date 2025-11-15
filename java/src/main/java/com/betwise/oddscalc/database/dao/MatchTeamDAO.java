/**
 * @author Owen Walton
 * Data Access Object for MatchTeam table in database
 */

package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
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
            statement.setString(1, matchTeam.getMatchID());
            statement.setString(2, matchTeam.getDataSource().name());
            statement.setInt(3, matchTeam.getTeamID());

            statement.executeUpdate();
            dbConnection.getConn().commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // insert a List of MatchTeam objects to DB
    // adds each singular object to the same batch before executing the entire batch to reduce querying
    // this increases efficiency and reduces points of failure
    @Override
    public void bulkInsertIfNotExists(List<MatchTeam> matchTeams) {
        if (matchTeams.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO MatchTeam (MatchID, DataSource, TeamID) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "MatchID = VALUES(MatchID), " +
                "DataSource = VALUES(DataSource), " +
                "TeamID = VALUES(TeamID)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (MatchTeam matchTeam : matchTeams) {
                statement.setString(1, matchTeam.getMatchID());
                statement.setString(2, matchTeam.getDataSource().name());
                statement.setInt(3, matchTeam.getTeamID());
                statement.addBatch();
            }

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
