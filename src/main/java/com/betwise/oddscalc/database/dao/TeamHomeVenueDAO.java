package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.TeamHomeVenue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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

    public void populateTeamHomeVenue() {
        // reset table
        String sql1 = "TRUNCATE TABLE CricketMatchData.TeamHomeVenue";
        // When 50% or more of a venues games have involved a team and there are 6 games (tests count for 2) or more,
        // the singular team with the most games there is assigned as the home team
        String sql2 = """
                 INSERT INTO CricketMatchData.TeamHomeVenue (TeamID, VenueID)
                 SELECT TeamID, VenueID FROM (
                    SELECT t.TeamID, v.VenueID, SUM(CASE WHEN cm.FormatName = 'Test' THEN 2 ELSE 1 END) AS weighted_games,
                    ROW_NUMBER() OVER (
                        PARTITION BY v.VenueID
                        ORDER BY SUM(CASE WHEN cm.FormatName = 'Test' THEN 2 ELSE 1 END) DESC
                    ) AS rn
                    FROM CricketMatchData.Venue v
                    JOIN CricketMatchData.CricketMatch cm
                        ON cm.VenueID = v.VenueID
                    JOIN CricketMatchData.MatchTeam mt
                        ON mt.MatchID = cm.MatchID
                        AND mt.DataSource = cm.DataSource
                    JOIN CricketMatchData.Team t
                        ON t.TeamID = mt.TeamID
                    GROUP BY v.VenueID, t.TeamID
                    HAVING SUM(CASE WHEN cm.FormatName = 'Test' THEN 2 ELSE 1 END) >= 5
                        AND SUM(CASE WHEN cm.FormatName = 'Test' THEN 2 ELSE 1 END) >=
                        0.5 * (
                                SELECT SUM(CASE WHEN cm2.FormatName = 'Test' THEN 2 ELSE 1 END)
                                FROM CricketMatchData.CricketMatch cm2
                                WHERE cm2.VenueID = v.VenueID
                            )
                 ) EachVenueRanked
                 WHERE rn = 1
                """;
        // relax the % of games for venues that still don't have a home team
        String sql3 = """
                 INSERT INTO CricketMatchData.TeamHomeVenue (TeamID, VenueID)
                 SELECT TeamID, VenueID
                 FROM (
                    SELECT t.TeamID, v.VenueID,
                        SUM(CASE WHEN cm.FormatName = 'Test' THEN 2 ELSE 1 END) AS weighted_games,
                        ROW_NUMBER() OVER (
                            PARTITION BY v.VenueID
                            ORDER BY SUM(CASE WHEN cm.FormatName = 'Test' THEN 2 ELSE 1 END) DESC
                        ) AS rn
                    FROM CricketMatchData.Venue v
                    JOIN CricketMatchData.CricketMatch cm
                        ON cm.VenueID = v.VenueID
                    JOIN CricketMatchData.MatchTeam mt
                        ON mt.MatchID = cm.MatchID AND mt.DataSource = cm.DataSource
                    JOIN CricketMatchData.Team t
                        ON t.TeamID = mt.TeamID
                    WHERE v.VenueID NOT IN (
                        SELECT VenueID FROM CricketMatchData.TeamHomeVenue
                    )
                    GROUP BY v.VenueID, t.TeamID
                    HAVING SUM(CASE WHEN cm.FormatName = 'Test' THEN 2 ELSE 1 END) >= 7
                        AND SUM(CASE WHEN cm.FormatName = 'Test' THEN 2 ELSE 1 END) >=
                            0.4 * (
                                SELECT SUM(CASE WHEN cm2.FormatName = 'Test' THEN 2 ELSE 1 END)
                                FROM CricketMatchData.CricketMatch cm2
                                WHERE cm2.VenueID = v.VenueID
                            )
                 ) EachVenueRanked
                 WHERE rn = 1
                """;

        Connection conn = dbConnection.getConn();
        try (Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);

            stmt.executeUpdate(sql1);
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignore) {
                System.err.println("Failed to rollback");
            }
            throw new RuntimeException("Failed to populate TeamHomeVenue", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
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

        String sql = "INSERT INTO TeamHomeVenue (TeamID, VenueID) " + "VALUES (?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {

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
