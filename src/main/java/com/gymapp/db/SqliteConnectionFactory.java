package com.gymapp.db;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

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
                dir = Paths.get(".").toAbsolutePath().normalize();
            }

            return dir.resolve(DB_NAME).toString();

        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to resolve DB path", e);
        }
    }

    private static final String DB_PATH = resolveDbPath();
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static String getUrl() {
        return URL;
    }

    public static Path getDbPath() {
        return Paths.get(DB_PATH);
    }

    @Override
    public Connection getConnection() {
        try {
            Path parent = getDbPath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to DB", e);
        }
    }
}