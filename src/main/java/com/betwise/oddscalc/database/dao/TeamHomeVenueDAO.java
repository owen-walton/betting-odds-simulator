package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.TeamHomeVenue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class TeamHomeVenueDAO implements WriteDAO<TeamHomeVenue>, AutoCloseable {

    private DBConnection dbConnection;

    // initialise connection
    public TeamHomeVenueDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException("Database connection failed");
        }
    }

    @Override
    public boolean insert(TeamHomeVenue teamHomeVenue) {
        String sql = "INSERT INTO TeamHomeVenue (TeamID, VenueID) VALUES (?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setInt(1, teamHomeVenue.teamID());
            statement.setInt(2, teamHomeVenue.venueID());

            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void bulkInsertIfNotExists(List<TeamHomeVenue> teamHomeVenues) {
        if (teamHomeVenues.isEmpty()) {
            return;
        }

        String sql = "INSERT IGNORE INTO TeamHomeVenue (TeamID, VenueID) " +
                "VALUES (?, ?)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (TeamHomeVenue teamHomeVenue : teamHomeVenues) {
                statement.setInt(1, teamHomeVenue.teamID());
                statement.setInt(2, teamHomeVenue.venueID());
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
