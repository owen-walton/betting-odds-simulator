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

    // insert a new PredictionModel row (ModelDate can be left null to use default CURRENT_TIMESTAMP)
    public void insert(PredictionModel model) {
        String sql = """
            INSERT INTO Cricket.PredictionModel
              (ModelDate, EValue, ELOGain,
               TossWinnerELOGainMultiplier, TossWinnerMultiplier,
               WinMarginMultiplier, HomeAdvantageMultiplier)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement ps = dbConnection.getConn().prepareStatement(sql)) {
            // If modelDate is null, let DB default to CURRENT_TIMESTAMP
            if (model.getModelDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(model.getModelDate()));
            } else {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            }
            ps.setDouble(2, model.geteValue());
            ps.setDouble(3, model.getEloGain());
            ps.setDouble(4, model.getTossWinnerEloGainMultiplier());
            ps.setDouble(5, model.getTossWinnerMultiplier());
            ps.setDouble(6, model.getWinMarginMultiplier());
            ps.setDouble(7, model.getHomeAdvantageMultiplier());
            ps.executeUpdate();
            dbConnection.getConn().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert PredictionModel", e);
        }
    }

    // ordered by ModelDate descending
    public List<PredictionModel> getAll() {
        List<PredictionModel> results = new ArrayList<>();
        String sql = """
            SELECT ModelID, ModelDate, EValue, ELOGain,
                   TossWinnerELOGainMultiplier, TossWinnerMultiplier,
                   WinMarginMultiplier, HomeAdvantageMultiplier
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
                model.seteValue((float) rs.getDouble("EValue"));
                model.setEloGain((float) rs.getDouble("ELOGain"));
                model.setTossWinnerEloGainMultiplier((float) rs.getDouble("TossWinnerELOGainMultiplier"));
                model.setTossWinnerMultiplier((float) rs.getDouble("TossWinnerMultiplier"));
                model.setWinMarginMultiplier((float) rs.getDouble("WinMarginMultiplier"));
                model.setHomeAdvantageMultiplier((float) rs.getDouble("HomeAdvantageMultiplier"));
                results.add(model);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve PredictionModel rows", e);
        }
        return results;
    }

    // Retrieve the most recent PredictionModel (latest ModelDate)
    public PredictionModel getLatestModel() {
        String sql = """
            SELECT ModelID, ModelDate, EValue, ELOGain,
                   TossWinnerELOGainMultiplier, TossWinnerMultiplier,
                   WinMarginMultiplier, HomeAdvantageMultiplier
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
                model.seteValue((float) rs.getDouble("EValue"));
                model.setEloGain((float) rs.getDouble("ELOGain"));
                model.setTossWinnerEloGainMultiplier((float) rs.getDouble("TossWinnerELOGainMultiplier"));
                model.setTossWinnerMultiplier((float) rs.getDouble("TossWinnerMultiplier"));
                model.setWinMarginMultiplier((float) rs.getDouble("WinMarginMultiplier"));
                model.setHomeAdvantageMultiplier((float) rs.getDouble("HomeAdvantageMultiplier"));
                return model;
            }
            return null; // no rows found
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve latest PredictionModel", e);
        }
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}
