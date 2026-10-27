package com.oop.project.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.sql.DataSource;

import com.oop.project.exception.DataAccessException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Builds the HikariCP connection pool from a properties file on the classpath.
 * The application uses application.properties; tests use
 * application-test.properties
 * (see TestDatabase). Neither file is committed: copy the matching *.example
 * file.
 */
public final class DatabaseConnection {

    public static final String DEFAULT_CONFIG = "application.properties";

    private static HikariDataSource shared;

    private DatabaseConnection() {
    }

    /** The pool shared by the whole application. Created on first use. */
    public static synchronized DataSource getDataSource() {
        if (shared == null) {
            shared = create(DEFAULT_CONFIG);
        }
        return shared;
    }

    /** Closes the shared pool (call when the application exits). */
    public static synchronized void close() {
        if (shared != null) {
            shared.close();
            shared = null;
        }
    }

    /**
     * Creates a new pool from the given classpath file. The caller must close it.
     */
    public static HikariDataSource create(String resourceName) {
        Properties props = load(resourceName);
        String url = require(props, "db.url", resourceName);
        String user = require(props, "db.user", resourceName);
        String password = props.getProperty("db.password", "");
        int poolSize = Integer.parseInt(props.getProperty("db.pool.size", "5").trim());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url.trim());
        config.setUsername(user.trim());
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        return new HikariDataSource(config);
    }

    private static Properties load(String resourceName) {
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new DataAccessException("Config file not found on the classpath: " + resourceName
                        + ". Copy " + resourceName + ".example to " + resourceName + " and fill in your settings.");
            }
            Properties props = new Properties();
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return props;
        } catch (IOException e) {
            throw new DataAccessException("Cannot read config file " + resourceName, e);
        }
    }

    private static String require(Properties props, String key, String resourceName) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new DataAccessException("Missing property '" + key + "' in " + resourceName);
        }
        return value;
    }
}