package com.betwise.oddscalc.database.connection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {

    final private String PROPERTIES_FILE = "db/DBConfig.properties";
    private String dbURL;
    private String dbUser;
    private String dbPassword;
    private Connection conn;

    public DBConnection() {
        this.conn = null;
    }

    private void loadProperties() {
        Properties dbConfig = new Properties();

        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("DBConfig.properties not found in classpath");
            }
            dbConfig.load(input);

            this.dbURL = dbConfig.getProperty("db.url");
            this.dbUser = dbConfig.getProperty("db.username");
            this.dbPassword = dbConfig.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load DB configuration", e);
        }
    }

    public void connect() {
        try {
            if (conn == null || conn.isClosed()) {
                loadProperties();
                // load the driver class explicitly
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(this.dbURL, this.dbUser, this.dbPassword);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (conn != null) {
            try {
                this.conn.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isConnected() {
        try {
            return (conn != null && !conn.isClosed());
        } catch (Exception e) {
            return false;
        }
    }

    public Connection getConn() {
        try {
            if (conn == null || conn.isClosed()) {
                connect();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}
