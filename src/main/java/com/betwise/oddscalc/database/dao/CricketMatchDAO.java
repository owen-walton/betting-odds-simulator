package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.CricketMatch;

import java.sql.PreparedStatement;

public class CricketMatchDAO implements WriteDAO<CricketMatch>, AutoCloseable{

    private DBConnection dbConnection;

    // initialise connection
    public CricketMatchDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean insert(CricketMatch cricketMatch) {
        String sql = "INSERT INTO CricketMatch (FormatName, DataSource, VenueID, StartDate) VALUES (?, ?, ?, ?)";

        // use prepared statement for SQL safety
        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, cricketMatch.getFormat().formatName()); // Assuming enum name matches FormatName
            statement.setString(2, cricketMatch.getDataSource().name());
            statement.setInt(3, cricketMatch.getVenueID());
            statement.setDate(4, java.sql.Date.valueOf(cricketMatch.getStartDate()));
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
