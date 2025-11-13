/**
 * @author Owen Walton
 * Allows the DDL to be run within the code
 * ,
 * @note: The JDBC connection is reliant on the CricketMatchData database
 * so it can only be used to overwrite a DB not initialise from scratch
 */
package com.betwise.oddscalc.database.initialise;

import com.betwise.oddscalc.database.connection.DBConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitialiser {

    // read queries from file then execute with JDBC
    public void runDDL() {
        DBConnection dbConnection = new DBConnection();
        String DDL_PATH_IN_RESOURCES = "/db/CricketMatchDataDDL.sql";

        try (Connection conn = dbConnection.getConn();
             Statement stmt = conn.createStatement()) {

            String ddl = readFileInResources(DDL_PATH_IN_RESOURCES);
            String[] statements = ddl.split(";");

            for (String s : statements) {
                s = s.trim();
                if (!s.isEmpty()) {
                    stmt.execute(s);
                }
            }
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // helper to read in the SQL queries from the file
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