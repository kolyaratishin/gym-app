package com.gymapp.db;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SqliteConnectionFactory implements ConnectionFactory {

    private static final String DB_NAME = "gym.db";

    private static String resolveDbPath() {
        try {
            Path appPath = Paths.get(
                    SqliteConnectionFactory.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            Path dir = appPath.getParent();

            if (dir != null) {
                dir = dir.getParent();
            }

            if (dir == null) {
                dir = Paths.get(".")
                        .toAbsolutePath()
                        .normalize();
            }

            return dir.resolve(DB_NAME).toString();

        } catch (URISyntaxException e) {
            throw new RuntimeException(
                    "Failed to resolve DB path",
                    e
            );
        }
    }

    private static final String DB_PATH =
            resolveDbPath();

    private static final String URL =
            "jdbc:sqlite:" + DB_PATH;

    public static String getUrl() {
        return URL;
    }

    public static Path getDbPath() {
        return Paths.get(DB_PATH);
    }

    @Override
    public Connection getConnection() {
        Connection connection = null;

        try {
            Path parent =
                    getDbPath().getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            connection =
                    DriverManager.getConnection(URL);

            enableForeignKeys(connection);
            verifyForeignKeysEnabled(connection);

            return connection;

        } catch (Exception e) {
            closeSilently(connection);

            throw new RuntimeException(
                    "Failed to connect to DB",
                    e
            );
        }
    }

    private void enableForeignKeys(
            Connection connection
    ) throws Exception {
        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );
        }
    }

    private void verifyForeignKeysEnabled(
            Connection connection
    ) throws Exception {
        try (
                Statement statement =
                        connection.createStatement();

                ResultSet resultSet =
                        statement.executeQuery(
                                "PRAGMA foreign_keys"
                        )
        ) {
            if (!resultSet.next()
                    || resultSet.getInt(1) != 1) {

                throw new IllegalStateException(
                        "SQLite foreign keys are not enabled"
                );
            }
        }
    }

    private void closeSilently(
            Connection connection
    ) {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (Exception ignored) {
            // Nothing to do.
        }
    }
}