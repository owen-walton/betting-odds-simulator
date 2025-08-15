package com.betwise.oddscalc.database.dao;

import com.betwise.oddscalc.database.connection.DBConnection;
import com.betwise.oddscalc.entity.VenueAlias;
import com.betwise.oddscalc.entity.VenueKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class VenueAliasDAO implements AutoCloseable {

    private final DBConnection dbConnection;

    public VenueAliasDAO() {
        this.dbConnection = new DBConnection();
        this.dbConnection.connect();
        if (!this.dbConnection.isConnected()) {
            throw new RuntimeException();
        }
    }

    public Map<VenueKey, Integer> loadAliasMap() {
        String sql = """
            SELECT AliasGroundName, AliasCity, VenueID
              FROM VenueAlias
            """;

        Map<VenueKey, Integer> aliasMap = new HashMap<>();

        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String ground = rs.getString("AliasGroundName").trim();
                String city   = rs.getString("AliasCity").trim();
                int venueId   = rs.getInt("VenueID");

                aliasMap.put(new VenueKey(ground, city), venueId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return aliasMap;
    }

    public boolean insert(VenueAlias alias) {
        String sql = """
            INSERT INTO VenueAlias
              (AliasGroundName, AliasCity, VenueID)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, alias.aliasGroundName().trim());
            ps.setString(2, alias.aliasCity().trim());
            ps.setInt(3, alias.venueId());
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // find alias' corresponding VenueID, or return -1 if not found.
    public int resolveVenueId(String aliasGround, String aliasCity) {
        String sql = """
            SELECT VenueID
              FROM VenueAlias
             WHERE AliasGroundName = ?
               AND AliasCity       = ?
            """;

        try (Connection conn = dbConnection.getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, aliasGround.trim());
            ps.setString(2, aliasCity.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("VenueID");
                }
                return -1;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        dbConnection.disconnect();
    }
}