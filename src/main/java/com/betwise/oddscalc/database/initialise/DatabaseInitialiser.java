package com.betwise.oddscalc.database.initialise;

import com.betwise.oddscalc.database.connection.DBConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitialiser {

    public void runDDL() {
        DBConnection dbConnection = new DBConnection();
        String DDL_PATH_IN_RESOURCES = "/db/CricketDDL.sql";

        try (Connection conn = dbConnection.getConn();
             Statement stmt = conn.createStatement()) {

            String ddl = readFileInResources(DDL_PATH_IN_RESOURCES);
            String[] statements = ddl.split(";");

            for (String s : statements) {
                s = s.trim();
                if (!s.isEmpty()) {
                    stmt.addBatch(s);
                }
            }

            stmt.executeBatch(); // execute all statements in one go

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String readFileInResources(String pathInResources) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = getClass().getResourceAsStream(pathInResources)) {
            if (is == null) {
                throw new IllegalStateException("DDL file not found: " + pathInResources);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue; // skip blank lines and comment lines
                    }
                    sb.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}