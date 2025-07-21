package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.Team;

import java.sql.PreparedStatement;

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

    @Override
    public boolean insert(Team team) {
        String sql = "INSERT INTO Team (Name) VALUES (?)";

        try (PreparedStatement statement = dbConnection.getConn().prepareStatement(sql)) {
            statement.setString(1, team.name());

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
