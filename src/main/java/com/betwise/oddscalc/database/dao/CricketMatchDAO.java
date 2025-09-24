package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

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

    public List<MatchDetails> getMatchDetails(String latestMatchID,
                                              DataSource latestDataSource,
                                              int batchSize) {
        List<MatchDetails> matchDetailsList = new ArrayList<>();

        String sql = """
        SELECT
            m.MatchID,
            m.DataSource,
            m.StartDate,
            r.WinningTeamID,
            r.TossWinningTeamID,
            r.TossDecision,
            r.Result,
            r.MarginSize,
            r.MarginType,
            mt.TeamID,
            t.ELO AS TeamElo,
            CASE WHEN thv.TeamID IS NOT NULL THEN TRUE ELSE FALSE END AS IsHome
        FROM Cricket.CricketMatch m
        JOIN Cricket.MatchTeam mt 
            ON m.MatchID = mt.MatchID AND m.DataSource = mt.DataSource
        JOIN Cricket.Team t
            ON mt.TeamID = t.TeamID
        LEFT JOIN Cricket.MatchResult r 
            ON m.MatchID = r.MatchID AND m.DataSource = r.DataSource
        LEFT JOIN Cricket.TeamHomeVenue thv 
            ON mt.TeamID = thv.TeamID 
            AND thv.VenueID = m.VenueID
        WHERE 
            (? IS NULL OR ? IS NULL
             OR (m.StartDate > (SELECT StartDate FROM Cricket.CricketMatch 
                                 WHERE MatchID = ? AND DataSource = ?)) 
             OR (m.StartDate = (SELECT StartDate FROM Cricket.CricketMatch 
                                 WHERE MatchID = ? AND DataSource = ?) 
                 AND m.MatchID > ?))
        ORDER BY m.StartDate ASC, m.MatchID ASC
        LIMIT ?
    """;

        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Parameters for default start (if latestMatchID or latestDataSource is null)
            ps.setString(1, latestMatchID);
            ps.setString(2, latestDataSource == null ? null : latestDataSource.name());
            ps.setString(3, latestMatchID);
            ps.setString(4, latestDataSource == null ? null : latestDataSource.name());
            ps.setString(5, latestMatchID);
            ps.setString(6, latestDataSource == null ? null : latestDataSource.name());
            ps.setString(7, latestMatchID);
            ps.setInt(8, batchSize);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                Map<String, MatchDetails> matchMap = new LinkedHashMap<>();

                while (rs.next()) {
                    String matchID = rs.getString("MatchID");
                    DataSource ds = DataSource.valueOf(rs.getString("DataSource"));

                    MatchDetails md = matchMap.get(matchID);
                    if (md == null) {
                        md = new MatchDetails();
                        md.setMatchID(matchID);
                        md.setDataSource(ds.name());
                        md.setStartDate(rs.getDate("StartDate").toLocalDate());
                        md.setWinningTeamID(rs.getObject("WinningTeamID") == null ? null : rs.getInt("WinningTeamID"));
                        md.setTossWinningTeamID(rs.getInt("TossWinningTeamID"));
                        md.setTossDecision(rs.getString("TossDecision"));
                        String result = rs.getString("Result");
                        md.setResult(result == null ? null : result.replace(' ', '_').replace('-', '_'));
                        md.setMarginSize(rs.getObject("MarginSize") == null ? 0 : rs.getInt("MarginSize"));
                        md.setMarginType(rs.getString("MarginType"));
                        md.setTeamHomeMap(new HashMap<>());
                        md.setTeamEloMap(new HashMap<>());
                        matchMap.put(matchID, md);
                    }

                    int teamID = rs.getInt("TeamID");
                    boolean isHome = rs.getBoolean("IsHome");
                    double elo = rs.getObject("TeamElo") == null ? 1500.0 : rs.getDouble("TeamElo");

                    md.getTeamHomeMap().put(teamID, isHome);
                    md.getTeamEloMap().put(teamID, elo);
                }

                matchDetailsList.addAll(matchMap.values());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch match details", e);
        }

        return matchDetailsList;
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }

    public static void main(String[] args) {
        try (CricketMatchDAO dao = new CricketMatchDAO()) {
            List<MatchDetails> matches = dao.getMatchDetails(null, null, 10);
            System.out.println("Fetched " + matches.size() + " matches");
            for (MatchDetails md : matches) {
                System.out.println("MatchID: " + md.getMatchID());
                System.out.println("StartDate: " + md.getStartDate());
                System.out.println("WinningTeamID: " + md.getWinningTeamID());
                System.out.println("TeamHomeMap: " + md.getTeamHomeMap());
                System.out.println("-------------------------");
            }
        }
    }

}
