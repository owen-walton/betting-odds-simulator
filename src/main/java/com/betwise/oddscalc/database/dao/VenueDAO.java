package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.Venue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    public List<Venue> getIDsIntoObjects(List<Venue> venues) {
        List<Venue> newList = new ArrayList<>();

        if (venues.isEmpty()) {
            return newList;
        }

        StringBuilder placeholdersBuilder = new StringBuilder();
        for (int i = 0; i < venues.size(); i++) {
            placeholdersBuilder.append("(?, ?)");
            if (i < venues.size() - 1) {
                placeholdersBuilder.append(", ");
            }
        }

        String sql = "SELECT VenueID, GroundName, City FROM Venue WHERE (GroundName, City) IN (" + placeholdersBuilder.toString() + ")";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            int index = 1;
            for (Venue venue : venues) {
                statement.setString(index++, venue.getGroundName());
                statement.setString(index++, venue.getCity());
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Venue venue = new Venue();
                    venue.setVenueID(rs.getInt("VenueID"));
                    venue.setGroundName(rs.getString("GroundName"));
                    venue.setCity(rs.getString("City"));
                    newList.add(venue);
                }
            }

            return newList;

        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public void bulkInsertIfNotExists(List<Venue> venues) {
        if (venues.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO Venue (GroundName, City) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE GroundName = VALUES(GroundName), City = VALUES(City)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            for (Venue venue : venues) {
                statement.setString(1, venue.getGroundName());
                statement.setString(2, venue.getCity());
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