
package com.oop.project.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class TestConnection {

    private static final String DEFAULT_URL =
            "jdbc:postgresql://localhost:5432/repair-workshop-queue?connectTimeout=5";

    public static void main(String[] args) {

        try {
            // Read configuration from .env in project root
            Map<String, String> env = loadEnv(Path.of(".env"));

            String url = getConfig("DB_URL", DEFAULT_URL, env);
            String username = getConfig("DB_USERNAME", null, env);
            String password = getConfig("DB_PASSWORD", null, env);

            if (username == null || username.isBlank()
                    || password == null || password.isBlank()) {
                throw new IllegalStateException(
                        "Missing DB_USERNAME or DB_PASSWORD in .env");
            }

            // Connect to PostgreSQL
            try (Connection conn =
                         DriverManager.getConnection(url, username, password)) {

                System.out.println("Connected successfully!");
                System.out.println("Database: " + conn.getCatalog());
                System.out.println("User: "
                        + conn.getMetaData().getUserName());

                // Query all rows from warranties
                String sql = "SELECT * FROM warranties";

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    System.out.println("\n========== WARRANTIES ==========");

                    // Print column names
                    for (int i = 1; i <= columnCount; i++) {
                        if (i > 1) {
                            System.out.print(" | ");
                        }
                        System.out.print(meta.getColumnLabel(i));
                    }
                    System.out.println();

                    System.out.println("-".repeat(100));

                    // Print records
                    int rowCount = 0;

                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) {
                                System.out.print(" | ");
                            }

                            Object value = rs.getObject(i);
                            System.out.print(
                                    value == null ? "NULL" : value.toString()
                            );
                        }

                        System.out.println();
                        rowCount++;
                    }

                    System.out.println("\nTotal warranties: " + rowCount);
                    System.out.println("===============================");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database operation failed!");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Reason: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }

    private static Map<String, String> loadEnv(Path path)
            throws Exception {
        Map<String, String> values = new HashMap<>();

        if (!Files.exists(path)) {
            throw new IllegalStateException(
                    "Cannot find .env: " + path.toAbsolutePath());
        }

        for (String line : Files.readAllLines(path)) {
            String s = line.trim();

            if (s.isEmpty() || s.startsWith("#")) {
                continue;
            }

            int pos = s.indexOf('=');
            if (pos <= 0) {
                continue;
            }

            String key = s.substring(0, pos).trim();
            String value = s.substring(pos + 1).trim();

            if (value.length() >= 2
                    && ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }

            values.put(key, value);
        }

        return values;
    }

    private static String getConfig(
            String name, String defaultValue,
            Map<String, String> env) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            value = env.get(name);
        }

        return value == null || value.isBlank()
                ? defaultValue : value;
    }
}