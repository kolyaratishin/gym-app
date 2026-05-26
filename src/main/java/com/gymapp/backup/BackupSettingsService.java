package com.gymapp.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class BackupSettingsService {

    private static final String EXTRA_BACKUP_PATH_KEY = "backup.extra.path";

    private final Path settingsFile;

    public BackupSettingsService() {
        this.settingsFile = Path.of("config", "app.properties");
    }

    public Optional<Path> getExtraBackupPath() {
        Properties properties = loadProperties();

        String value = properties.getProperty(EXTRA_BACKUP_PATH_KEY);

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(Path.of(value.trim()));
    }

    public void saveExtraBackupPath(String path) {
        Properties properties = loadProperties();

        if (path == null || path.isBlank()) {
            properties.remove(EXTRA_BACKUP_PATH_KEY);
        } else {
            properties.setProperty(EXTRA_BACKUP_PATH_KEY, path.trim());
        }

        saveProperties(properties);
    }

    public void clearExtraBackupPath() {
        saveExtraBackupPath(null);
    }

    private Properties loadProperties() {
        try {
            ensureConfigDirectoryExists();

            Properties properties = new Properties();

            if (Files.exists(settingsFile)) {
                try (InputStream inputStream = Files.newInputStream(settingsFile)) {
                    properties.load(inputStream);
                }
            }

            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load backup settings", e);
        }
    }

    private void saveProperties(Properties properties) {
        try {
            ensureConfigDirectoryExists();

            try (OutputStream outputStream = Files.newOutputStream(settingsFile)) {
                properties.store(outputStream, "Gym App settings");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save backup settings", e);
        }
    }

    private void ensureConfigDirectoryExists() throws IOException {
        Files.createDirectories(settingsFile.getParent());
    }
}