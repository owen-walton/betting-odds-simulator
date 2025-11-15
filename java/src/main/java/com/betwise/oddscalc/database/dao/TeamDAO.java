/**
 * @author Owen Walton
 * Data Access Object for Team table in database
 */

package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.Team;
import com.betwise.oddscalc.entity.TeamKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class TeamDAO implements WriteDAO<Team>, AutoCloseable {

    private DBConnection dbConnection;

    // initialise connection
    public TeamDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException("Database connection failed");
        }
    }

    // takes in a list of Team objects with filler/unassigned ids, e.g. -1
    // Uses the natural key of the object (name) to find the true ids from the db
    // Then build a new list of the correct Team objects
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
        String sql = "SELECT TeamID, Name FROM Team WHERE Name IN (" + placeholdersBuilder.toString() + ")";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            int index = 1;
            for (Team team : teams) {
                statement.setString(index, team.getName());
                index++;
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
        String sql = "INSERT INTO Team (Name) VALUES (?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, team.getName());

            statement.executeUpdate();
            dbConnection.getConn().commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // receives a list of Team objects and remove all duplicates (keep first one then remove rest),
    // then for the Teams still in list, search db (by natural key) for each one,
    // if not found add it to a new list then return the new list
    public List<Team> removeExisting(List<Team> teams) {
        if (teams.isEmpty()) {
            return new ArrayList<>();
        }

        // remove all duplicate team appearances in list
        Set<TeamKey> uniqueKeys = new HashSet<>();
        List<Team> uniqueTeams = new ArrayList<>();
        for (Team team : teams) {
            TeamKey key = new TeamKey(team.getName());
            if (!uniqueKeys.contains(key)) {
                uniqueKeys.add(key);
                uniqueTeams.add(team);
            }
        }
        teams = uniqueTeams;

        // remove all that appear in db
        // prepare query string
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
                ps.setString(i, team.getName());
                i++;
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

    // insert a List of Team objects to DB
    // adds each singular object to the same batch before executing the entire batch to reduce querying
    // this increases efficiency and reduces points of failure
    @Override
    public void bulkInsertIfNotExists(List<Team> teams) {
        teams = removeExisting(teams);

        if (teams.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO Team (Name) VALUES (?)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            // build bulk insert
            for (Team team : teams) {
                preparedStatement.setString(1, team.getName());
                preparedStatement.addBatch();
            }

            // executed bulk insert
            preparedStatement.executeBatch();
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
