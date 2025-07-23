package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.Venue;

import java.sql.PreparedStatement;

public class VenueDAO implements WriteDAO<Venue>, AutoCloseable {

    private DBConnection dbConnection;

    // Initialise connection
    public VenueDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException("Database connection failed");
        }
    }

    @Override
    public boolean insert(Venue venue) {
        String sql = "INSERT INTO Venue (GroundName, City) VALUES (?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, venue.getGroundName());
            statement.setString(2, venue.getCity());

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