package com.oop.project.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.oop.project.config.DatabaseConnection;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Test database helper. Reads src/test/resources/application-test.properties,
 * refuses to run unless the database name ends with "_test", and can rebuild
 * the
 * schema and seed data so every test starts from the same clean state.
 */
public final class TestDatabase {

    private static final String CONFIG = "application-test.properties";
    private static HikariDataSource dataSource;

    private TestDatabase() {
    }

    public static synchronized DataSource dataSource() {
        if (dataSource == null) {
            HikariDataSource ds = DatabaseConnection.create(CONFIG);
            String url = ds.getJdbcUrl();
            if (!url.endsWith("_test")) {
                ds.close();
                throw new IllegalStateException("Refusing to run tests on '" + url
                        + "': the test database name must end with _test");
            }
            dataSource = ds;
        }
        return dataSource;
    }

    /** Drops and recreates every table, then loads the seed data. */
    public static void reset() {
        run("db/schema.sql");
        run("db/seed_data.sql");
    }

    private static void run(String resource) {
        String sql = read(resource);
        try (Connection c = dataSource().getConnection(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot run " + resource + ": " + e.getMessage(), e);
        }
    }

    private static String read(String resource) {
        try (InputStream in = TestDatabase.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on the classpath: " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + resource, e);
        }
    }
}