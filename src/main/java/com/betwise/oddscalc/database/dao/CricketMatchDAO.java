package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.CricketMatch;
import com.betwise.oddscalc.entity.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

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

    public java.sql.Date getMostRecentMatchDate() {
        String sql = "SELECT MAX(StartDate) AS MostRecent FROM CricketMatch";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                java.sql.Date date = rs.getDate("MostRecent");
                if (date != null) {
                    return date;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return java.sql.Date.valueOf("1970-01-01"); // no matches so use a 'minimum' date
    }

    @Override
    public boolean insert(CricketMatch cricketMatch) {
        String sql = "INSERT INTO CricketMatch (MatchID, DataSource, FormatName, VenueID, StartDate) VALUES (?, ?, ?, ?, ?)";

        // use prepared statement for SQL safety
        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, cricketMatch.getMatchID());
            statement.setString(2, cricketMatch.getDataSource().name());
            statement.setString(3, cricketMatch.getFormatName()); // Assuming enum name matches FormatName
            statement.setInt(4, cricketMatch.getVenueID());
            statement.setDate(5, java.sql.Date.valueOf(cricketMatch.getStartDate()));
            statement.executeUpdate();
            dbConnection.getConn().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void bulkInsertIfNotExists(List<CricketMatch> cricketMatches) {
        if (cricketMatches.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO CricketMatch (MatchID, DataSource, FormatName, VenueID, StartDate) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "DataSource = VALUES(DataSource)," +
                "FormatName = VALUES(FormatName)," +
                "VenueID = VALUES(VenueID)," +
                "StartDate = VALUES(StartDate)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            // build bulk insert
            for (CricketMatch cricketMatch : cricketMatches) {
                statement.setString(1, cricketMatch.getMatchID());
                statement.setString(2, cricketMatch.getDataSource().name());
                statement.setString(3, cricketMatch.getFormatName()); // Assuming enum name matches FormatName
                statement.setInt(4, cricketMatch.getVenueID());
                statement.setDate(5, java.sql.Date.valueOf(cricketMatch.getStartDate()));
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
