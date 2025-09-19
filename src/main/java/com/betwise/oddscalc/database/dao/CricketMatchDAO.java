package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public CricketMatchDataSchema getMatch(Map.Entry<DataSource, String> matchID) {
        CricketMatchDataSchema schema = new CricketMatchDataSchema();

        try (Connection conn = dbConnection.getConn()) {

            // ---- Fetch the CricketMatch itself ----
            String matchSql =
                    "SELECT MatchID, DataSource, FormatName, VenueID, StartDate " +
                            "FROM Cricket.CricketMatch WHERE MatchID = ? AND DataSource = ?";
            try (PreparedStatement ps = conn.prepareStatement(matchSql)) {
                ps.setString(1, matchID.getValue());
                ps.setString(2, matchID.getKey().name());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        CricketMatch cm = new CricketMatch();
                        cm.setMatchID(rs.getString("MatchID"));
                        cm.setDataSource(DataSource.valueOf(rs.getString("DataSource")));
                        cm.setFormatName(rs.getString("FormatName"));
                        cm.setVenueID(rs.getInt("VenueID"));
                        cm.setStartDate(rs.getDate("StartDate").toLocalDate());
                        schema.getCricketMatches().add(cm);
                    }
                }
            }

            // ---- Fetch Teams playing the match ----
            String teamSql =
                    "SELECT t.TeamID, t.Name, t.ELO " +
                            "FROM Cricket.Team t " +
                            "JOIN Cricket.MatchTeam mt ON t.TeamID = mt.TeamID " +
                            "WHERE mt.MatchID = ? AND mt.DataSource = ?";
            try (PreparedStatement ps = conn.prepareStatement(teamSql)) {
                ps.setString(1, matchID.getValue());
                ps.setString(2, matchID.getKey().name());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Team team = new Team(
                                rs.getInt("TeamID"),
                                rs.getString("Name"),
                                rs.getObject("ELO") == null ? 1500 : rs.getFloat("ELO")
                        );
                        schema.getTeams().add(team);
                    }
                }
            }

            // ---- Fetch Venue ----
            String venueSql =
                    "SELECT v.VenueID, v.GroundName, v.City " +
                            "FROM Cricket.Venue v " +
                            "JOIN Cricket.CricketMatch m ON v.VenueID = m.VenueID " +
                            "WHERE m.MatchID = ? AND m.DataSource = ?";
            try (PreparedStatement ps = conn.prepareStatement(venueSql)) {
                ps.setString(1, matchID.getValue());
                ps.setString(2, matchID.getKey().name());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Venue v = new Venue(
                                rs.getInt("VenueID"),
                                rs.getString("GroundName"),
                                rs.getString("City")
                        );
                        schema.getVenues().add(v);
                    }
                }
            }

            // ---- Fetch TeamHomeVenues ----
            String thvSql =
                    "SELECT TeamHomeVenueID, TeamID, VenueID " +
                            "FROM Cricket.TeamHomeVenue";
            try (PreparedStatement ps = conn.prepareStatement(thvSql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TeamHomeVenue thv = new TeamHomeVenue(
                            rs.getInt("TeamHomeVenueID"),
                            rs.getInt("TeamID"),
                            rs.getInt("VenueID")
                    );
                    schema.getTeamHomeVenues().add(thv);
                }
            }

            // ---- Fetch MatchResults ----
            String resultSql =
                    "SELECT MatchID, DataSource, WinningTeamID, TossWinningTeamID, TossDecision, " +
                            "Result, MarginSize, MarginType " +
                            "FROM Cricket.MatchResult WHERE MatchID = ? AND DataSource = ?";
            try (PreparedStatement ps = conn.prepareStatement(resultSql)) {
                ps.setString(1, matchID.getValue());
                ps.setString(2, matchID.getKey().name());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MatchResult mr = new MatchResult();
                        mr.setMatchID(rs.getString("MatchID"));
                        mr.setDataSource(DataSource.valueOf(rs.getString("DataSource")));
                        Integer winId = rs.getObject("WinningTeamID") == null ? null : rs.getInt("WinningTeamID");
                        mr.setWinningTeamID(winId);
                        mr.setTossWinningTeamID(rs.getInt("TossWinningTeamID"));
                        mr.setTossDecision(TossDecision.valueOf(rs.getString("TossDecision")));
                        String res = rs.getString("Result");
                        mr.setResult(res == null ? null : Result.fromString(res.replace(' ', '_').replace('-', '_')));
                        Integer margin = rs.getObject("MarginSize") == null ? null : rs.getInt("MarginSize");
                        mr.setMarginSize(margin);
                        String mType = rs.getString("MarginType");
                        mr.setMarginType(mType == null ? null : MarginType.valueOf(mType.replace(' ', '_')));
                        schema.getMatchResults().add(mr);
                    }
                }
            }

            // ---- Fetch MatchTeams ----
            String matchTeamSql =
                    "SELECT MatchTeamID, MatchID, DataSource, TeamID " +
                            "FROM Cricket.MatchTeam WHERE MatchID = ? AND DataSource = ?";
            try (PreparedStatement ps = conn.prepareStatement(matchTeamSql)) {
                ps.setString(1, matchID.getValue());
                ps.setString(2, matchID.getKey().name());
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MatchTeam mt = new MatchTeam();
                        mt.setMatchTeamID(rs.getInt("MatchTeamID"));
                        mt.setMatchID(rs.getString("MatchID"));
                        mt.setDataSource(DataSource.valueOf(rs.getString("DataSource")));
                        mt.setTeamID(rs.getInt("TeamID"));
                        schema.getMatchTeams().add(mt);
                    }
                }
            }

            // ---- Fetch MatchFormats (lookup only) ----
            String formatSql = "SELECT FormatName, MatchLengthDays FROM Cricket.MatchFormat";
            try (PreparedStatement ps = conn.prepareStatement(formatSql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchFormat mf = new MatchFormat(
                            rs.getString("FormatName"),
                            rs.getInt("MatchLengthDays")
                    );
                    if (schema.getMatchFormats() == null) {
                        schema.setMatchFormats(new java.util.ArrayList<>());
                    }
                    schema.getMatchFormats().add(mf);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error fetching match data", e);
        }

        return schema;
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
