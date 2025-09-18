package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.AppliesTo;
import com.betwise.oddscalc.entity.FactorApplication;
import com.betwise.oddscalc.entity.Operation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FactorApplicationDAO implements AutoCloseable {

    private final DBConnection dbConnection;

    public FactorApplicationDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException("Database connection failed");
        }
    }

    // clear table
    public void clear() {
        String sql = "TRUNCATE TABLE Cricket.FactorApplication";
        try (PreparedStatement ps = dbConnection.getConn().prepareStatement(sql)) {
            ps.executeUpdate();
            dbConnection.getConn().commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear FactorApplication table", e);
        }
    }

    // batch insert/update
    public void bulkUpsertIfNotExists(List<FactorApplication> list) {
        if (list == null || list.isEmpty()) return;

        String sql = """
            INSERT INTO Cricket.FactorApplication
              (ApplyOrder, AppliesTo, FactorID, FactorValue, Operation)
            VALUES (?, ?, (SELECT FactorID FROM Cricket.Factors WHERE FactorName = ?), ?, ?)
            ON DUPLICATE KEY UPDATE
              FactorID = VALUES(FactorID),
              FactorValue = VALUES(FactorValue),
              Operation = VALUES(Operation)
            """;

        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (FactorApplication fa : list) {
                ps.setInt(1, fa.getApplyOrder());
                ps.setString(2, fa.getAppliesTo().toString());
                ps.setString(3, fa.getFactorName());
                ps.setDouble(4, fa.getFactorValue());
                ps.setString(5, fa.getOperation().getDbValue());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // retrieve all FactorApplication rows joined with their FactorName
    public List<FactorApplication> getAll() {
        List<FactorApplication> results = new ArrayList<>();
        String sql = """
            SELECT fa.ApplyOrder,
                   fa.AppliesTo,
                   f.FactorName,
                   fa.FactorValue,
                   fa.Operation
            FROM Cricket.FactorApplication fa
            JOIN Cricket.Factors f ON fa.FactorID = f.FactorID
            ORDER BY fa.ApplyOrder
            """;

        try (PreparedStatement ps = dbConnection.getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                FactorApplication fa = new FactorApplication();
                fa.setApplyOrder(rs.getInt("ApplyOrder"));
                fa.setAppliesTo(AppliesTo.valueOf(rs.getString("AppliesTo")));
                fa.setFactorName(rs.getString("FactorName"));
                fa.setFactorValue((float)rs.getDouble("FactorValue"));
                fa.setOperation(Operation.valueOf(rs.getString("Operation")));
                results.add(fa);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}
