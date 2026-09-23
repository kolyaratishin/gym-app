package com.gymapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class AppConfigService {

    private static final String TELEGRAM_TOKEN_KEY =
            "telegram.bot.token";

    private static final String TELEGRAM_TOKEN_ENV =
            "TELEGRAM_BOT_TOKEN";

    private final Path configFile;

    public AppConfigService() {
        this.configFile = resolveConfigFile();
    }

    public String getTelegramToken() {
        String configuredToken = getProperty(TELEGRAM_TOKEN_KEY);

        if (configuredToken != null && !configuredToken.isBlank()) {
            return configuredToken.trim();
        }

        String envToken = System.getenv(TELEGRAM_TOKEN_ENV);

        if (envToken != null && !envToken.isBlank()) {
            return envToken.trim();
        }

        return null;
    }

    public boolean hasTelegramToken() {
        return getTelegramToken() != null;
    }

    public void saveTelegramToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Telegram token cannot be empty"
            );
        }

        Properties properties = loadProperties();

        properties.setProperty(
                TELEGRAM_TOKEN_KEY,
                token.trim()
        );

        saveProperties(properties);
    }

    public void deleteTelegramToken() {
        Properties properties = loadProperties();

        if (properties.remove(TELEGRAM_TOKEN_KEY) != null) {
            saveProperties(properties);
        }
    }

    public boolean hasStoredTelegramToken() {
        String token = getProperty(TELEGRAM_TOKEN_KEY);

        return token != null && !token.isBlank();
    }

    public boolean isTelegramTokenFromEnvironment() {
        if (hasStoredTelegramToken()) {
            return false;
        }

        String envToken = System.getenv(TELEGRAM_TOKEN_ENV);

        return envToken != null && !envToken.isBlank();
    }

    public Path getConfigFile() {
        return configFile;
    }

    private String getProperty(String key) {
        return loadProperties().getProperty(key);
    }

    private Properties loadProperties() {
        Properties properties = new Properties();

        if (!Files.exists(configFile)) {
            return properties;
        }

        try (InputStream inputStream =
                     Files.newInputStream(configFile)) {

            properties.load(inputStream);
            return properties;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read application config: "
                            + configFile,
                    e
            );
        }
    }

    private void saveProperties(Properties properties) {
        try {
            Files.createDirectories(
                    configFile.getParent()
            );

            try (OutputStream outputStream =
                         Files.newOutputStream(
                                 configFile,
                                 StandardOpenOption.CREATE,
                                 StandardOpenOption.TRUNCATE_EXISTING
                         )) {

                properties.store(
                        outputStream,
                        "Gym App configuration"
                );
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save application config: "
                            + configFile,
                    e
            );
        }
    }

    private Path resolveConfigFile() {
        String localAppData =
                System.getenv("LOCALAPPDATA");

        if (localAppData != null
                && !localAppData.isBlank()) {

            return Path.of(
                    localAppData,
                    "GymApp",
                    "config.properties"
            );
        }

        return Path.of(
                System.getProperty("user.home"),
                ".gymapp",
                "config.properties"
        );
    }
}