package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.PredictionModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PredictionModelDAO implements AutoCloseable {

    private final DBConnection dbConnection;

    public PredictionModelDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException("Database connection failed");
        }
    }

    // insert a new row (ModelDate can be null to use CURRENT_TIMESTAMP)
    public void insert(PredictionModel model) {
        String sql = """
            INSERT INTO Cricket.PredictionModel
              (ModelDate, EValue, HomeAdvantageMultiplier)
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement ps = dbConnection.getConn().prepareStatement(sql)) {
            if (model.getModelDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(model.getModelDate()));
            } else {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            }
            ps.setDouble(2, model.getEValue());
            ps.setDouble(3, model.getHomeAdvantageMultiplier());
            ps.executeUpdate();
            dbConnection.getConn().commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert PredictionModel", e);
        }
    }

    // get all rows ordered by ModelDate
    public List<PredictionModel> getAll() {
        List<PredictionModel> results = new ArrayList<>();
        String sql = """
            SELECT ModelID, ModelDate, EValue, HomeAdvantageMultiplier
            FROM Cricket.PredictionModel
            ORDER BY ModelDate DESC
            """;

        try (PreparedStatement ps = dbConnection.getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PredictionModel model = new PredictionModel();
                model.setModelId(rs.getInt("ModelID"));
                Timestamp ts = rs.getTimestamp("ModelDate");
                model.setModelDate(ts != null ? ts.toLocalDateTime() : null);
                model.setEValue((float) rs.getDouble("EValue"));
                model.setHomeAdvantageMultiplier((float) rs.getDouble("HomeAdvantageMultiplier"));
                results.add(model);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve PredictionModel rows", e);
        }
        return results;
    }

    // get the most recent row
    public PredictionModel getLatestModel() {
        String sql = """
            SELECT ModelID, ModelDate, EValue, HomeAdvantageMultiplier
            FROM Cricket.PredictionModel
            ORDER BY ModelDate DESC
            LIMIT 1
            """;

        try (PreparedStatement ps = dbConnection.getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                PredictionModel model = new PredictionModel();
                model.setModelId(rs.getInt("ModelID"));
                Timestamp ts = rs.getTimestamp("ModelDate");
                model.setModelDate(ts != null ? ts.toLocalDateTime() : null);
                model.setEValue((float) rs.getDouble("EValue"));
                model.setHomeAdvantageMultiplier((float) rs.getDouble("HomeAdvantageMultiplier"));
                return model;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve latest PredictionModel", e);
        }
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}
