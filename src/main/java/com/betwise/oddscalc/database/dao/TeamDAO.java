package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.Team;
import com.betwise.oddscalc.entity.TeamKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class TeamDAO implements WriteDAO<Team>, AutoCloseable {

    private final DBConnection dbConnection;

    // initialise connection
    public TeamDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException("Database connection failed");
        }
    }

    public List<Team> getIDsIntoObjects(List<Team> teams) {
        List<Team> newList = new ArrayList<>();

        if (teams.isEmpty()) {
            return newList;
        }

        StringBuilder placeholdersBuilder = new StringBuilder();
        for (int i = 0; i < teams.size(); i++) {
            placeholdersBuilder.append("?");
            if (i < teams.size() - 1) {
                placeholdersBuilder.append(", ");
            }
        }
        String sql = "SELECT TeamID, Name FROM Team WHERE Name IN (" + placeholdersBuilder + ")";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            int index = 1;
            for (Team team : teams) {
                statement.setString(index++, team.getName());
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Team team = new Team();
                    team.setTeamID(rs.getInt("TeamID"));
                    team.setName(rs.getString("Name"));
                    newList.add(team);
                }
            }

            return newList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getAllTeamNames() {
        Set<String> names = new HashSet<>();
        String sql = "SELECT Name FROM Team";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                names.add(rs.getString("Name"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load all team names", e);
        }

        return names;
    }

    @Override
    public boolean insert(Team team) {
        String sql = "INSERT INTO Team (Name, ELO) VALUES (?, ?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, team.getName());
            Float elo = team.getElo();
            if (elo != null) {
                statement.setFloat(2, elo);
            } else {
                statement.setNull(2, java.sql.Types.FLOAT);
            }

            statement.executeUpdate();
            dbConnection.getConn().commit();
            return true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to insert team", e);
        }
    }

    public List<Team> removeExisting(List<Team> teams) {
        if (teams.isEmpty()) {
            return new ArrayList<>();
        }

        // remove all duplicate team appearances in list
        Set<TeamKey> uniqueKeys = new HashSet<>();
        List<Team> uniqueTeams = new ArrayList<>();
        for (Team team : teams) {
            TeamKey key = new TeamKey(team.getName());
            if (uniqueKeys.add(key)) {
                uniqueTeams.add(team);
            }
        }
        teams = uniqueTeams;

        // prepare string to query existing names
        StringBuilder sb = new StringBuilder("SELECT Name FROM Team WHERE Name IN (");
        for (int i = 0; i < teams.size(); i++) {
            sb.append("?");
            if (i < teams.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        String sql = sb.toString();

        // query existing names
        Set<TeamKey> existingTeamKeys = new HashSet<>();
        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            for (Team team : teams) {
                ps.setString(i++, team.getName());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existingTeamKeys.add(new TeamKey(rs.getString("Name")));
                }
            }

            List<Team> newTeams = new ArrayList<>();
            for (Team team : teams) {
                if (!existingTeamKeys.contains(new TeamKey(team.getName()))) {
                    newTeams.add(team);
                }
            }
            return newTeams;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void bulkInsertIfNotExists(List<Team> teams) {
        teams = removeExisting(teams);

        if (teams.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO Team (Name, ELO) VALUES (?, ?)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            for (Team team : teams) {
                preparedStatement.setString(1, team.getName());
                Float elo = team.getElo();
                if (elo != null) {
                    preparedStatement.setFloat(2, elo);
                } else {
                    preparedStatement.setNull(2, java.sql.Types.FLOAT);
                }
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateElo(TeamKey key, Float elo) {
        String sql = "UPDATE Team SET ELO = ? WHERE LOWER(TRIM(Name)) = LOWER(TRIM(?))";

        try (PreparedStatement ps = dbConnection.getConn().prepareStatement(sql)) {
            if (elo != null) {
                ps.setFloat(1, elo);
            } else {
                ps.setNull(1, java.sql.Types.FLOAT);
            }
            ps.setString(2, key.name());
            ps.executeUpdate();
            dbConnection.getConn().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ELO for team " + key.name(), e);
        }
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}
