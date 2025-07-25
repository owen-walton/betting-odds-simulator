package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.Team;
import com.betwise.oddscalc.entity.TeamKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public boolean insert(Team team) {
        String sql = "INSERT INTO Team (Name) VALUES (?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, team.getName());

            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void bulkInsertIfNotExists(List<Team> teams) {
        if (teams.isEmpty()) {
            return;
        }

        String sql = "INSERT IGNORE INTO Team (Name) VALUES (?)";

        try (Connection conn = dbConnection.getConn();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            // build bulk insert
            for (Team team : teams) {
                preparedStatement.setString(1, team.getName());
                preparedStatement.addBatch();
            }

            // executed bulk insert
            preparedStatement.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}
