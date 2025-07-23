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
        String sql = "INSERT INTO CricketMatch (MatchID, DataSource, FormatName, VenueID, StartDate) VALUES (?, ?, ?, ?, ?)";

        // use prepared statement for SQL safety
        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setInt(1, cricketMatch.getMatchID());
            statement.setString(2, cricketMatch.getDataSource().name());
            statement.setString(3, cricketMatch.getFormatName()); // Assuming enum name matches FormatName
            statement.setInt(4, cricketMatch.getVenueID());
            statement.setDate(5, java.sql.Date.valueOf(cricketMatch.getStartDate()));
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
