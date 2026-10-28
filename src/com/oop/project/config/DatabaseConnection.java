package com.oop.project.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import com.oop.project.exception.DataAccessException;

/**
 * Creates the PostgreSQL data source from environment variables or the root
 * .env file. Environment variables take precedence.
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:postgresql://localhost:5432/repair-workshop-queue";

    private static DataSource shared;

    private DatabaseConnection() {
    }

    /** The data source shared by the whole application. Created on first use. */
    public static synchronized DataSource getDataSource() {
        if (shared == null) {
            shared = createFromEnvironment();
        }
        return shared;
    }

    /** Clears the shared data source. PostgreSQL connections are closed per use. */
    public static synchronized void close() {
        shared = null;
    }

    private static DataSource createFromEnvironment() {
        Map<String, String> env = loadEnv();
        String url = getConfig("DB_URL", DEFAULT_URL, env);
        String username = getConfig("DB_USERNAME", null, env);
        String password = getConfig("DB_PASSWORD", null, env);

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new DataAccessException(
                    "Missing DB_USERNAME or DB_PASSWORD. Set them in environment variables or the project-root .env file.");
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    private static Map<String, String> loadEnv() {
        Path path = Path.of(".env");
        if (!Files.exists(path)) {
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String entry = line.trim();
                if (entry.isEmpty() || entry.startsWith("#")) {
                    continue;
                }

                int separator = entry.indexOf('=');
                if (separator <= 0) {
                    continue;
                }

                String key = entry.substring(0, separator).trim();
                String value = entry.substring(separator + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException e) {
            throw new DataAccessException("Cannot read the project-root .env file.", e);
        }
        return values;
    }

    private static String getConfig(String key, String defaultValue, Map<String, String> env) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = env.get(key);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }
}