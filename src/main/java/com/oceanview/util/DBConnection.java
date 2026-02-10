package com.oceanview.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;
    private final Connection connection;

    private DBConnection() {
        try {
            String driver = System.getProperty("DB_DRIVER");
            String url = System.getProperty("DB_URL");
            String user = System.getProperty("DB_USER");
            String password = System.getProperty("DB_PASSWORD");
            // CONNECT
            Class.forName(driver);
            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("OceanView Database Connected!");

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB Connection Failed: " + e.getMessage());
        }
    }

    // Public method to get the single instance
    public static synchronized DBConnection getInstance() {
        try {
            if (instance == null || instance.getConnection().isClosed()) {
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}

