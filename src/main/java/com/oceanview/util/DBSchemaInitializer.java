package com.oceanview.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DBSchemaInitializer {

    private static final String[] SCRIPTS = {
            "reservation_status_audit_trigger.sql",
            "cancel_reservation.sql"
    };

    public static void runAll() {
        for (String scriptName : SCRIPTS) {
            try {
                runScript(scriptName);
                System.out.println("OceanView DB Script applied: " + scriptName);
            } catch (Exception e) {
                // Log but do NOT crash startup — the object may already exist
                System.err.println("OceanView DB Script warning [" + scriptName + "]: " + e.getMessage());
            }
        }
    }

    private static void runScript(String scriptName) throws Exception {
        String fullSql;
        try (InputStream is = DBSchemaInitializer.class.getClassLoader().getResourceAsStream(scriptName)) {
            if (is == null) {
                throw new IllegalStateException("Script not found on classpath: " + scriptName);
            }
            fullSql = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        }

        String normalized = fullSql
                .replaceAll("(?i)DELIMITER\\s+\\$\\$", "")
                .replaceAll("(?i)DELIMITER\\s+;", "")
                .trim();

        // Split on $$ which separates compound statements
        String[] blocks = normalized.split("\\$\\$");

        Connection conn = DBConnection.getInstance().getConnection();
        for (String block : blocks) {
            String stmt = block.trim();
            if (stmt.isEmpty())
                continue;
            // Remove trailing semicolon from simple statements
            if (stmt.endsWith(";")) {
                stmt = stmt.substring(0, stmt.length() - 1).trim();
            }
            try (Statement s = conn.createStatement()) {
                s.execute(stmt);
            } catch (SQLException e) {
                // "already exists" errors are safe to ignore (error codes 1304 = proc, 1050 =
                // table, 1359 = trigger)
                int code = e.getErrorCode();
                if (code == 1304 || code == 1050 || code == 1359) {
                    System.out.println("OceanView DB [" + scriptName + "] already exists – skipping.");
                } else {
                    throw e;
                }
            }
        }
    }
}
