package com.gymapp.backup;

import com.gymapp.db.SqliteConnectionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BackupService {

    private static final DateTimeFormatter BACKUP_NAME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final int MAX_BACKUPS_TO_KEEP = 20;

    private final BackupSettingsService backupSettingsService;

    public BackupService(BackupSettingsService backupSettingsService) {
        this.backupSettingsService = backupSettingsService;
    }

    public Path createLocalBackup() {
        Path dbPath = SqliteConnectionFactory.getDbPath();

        if (!Files.exists(dbPath)) {
            throw new RuntimeException("Database file not found: " + dbPath);
        }

        Path backupDir = resolveBackupDir(dbPath);

        try {
            Files.createDirectories(backupDir);

            String timestamp = LocalDateTime.now().format(BACKUP_NAME_FORMAT);
            Path backupFile = backupDir.resolve("gym-backup-" + timestamp + ".db");

            Files.copy(dbPath, backupFile, StandardCopyOption.REPLACE_EXISTING);

            copyToExtraBackupPathIfConfigured(backupFile);
            deleteOldBackupsIfNeeded();

            return backupFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create local backup", e);
        }
    }

    public List<Path> listBackups() {
        Path backupDir = resolveBackupDir(SqliteConnectionFactory.getDbPath());

        if (!Files.exists(backupDir)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(backupDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".db"))
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list backups", e);
        }
    }

    public void restoreBackup(Path backupFile) {
        if (backupFile == null || !Files.exists(backupFile)) {
            throw new RuntimeException("Backup file not found: " + backupFile);
        }

        Path dbPath = SqliteConnectionFactory.getDbPath();

        try {
            Path parent = dbPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.copy(backupFile, dbPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to restore backup", e);
        }
    }

    public Optional<Path> getExtraBackupPath() {
        return backupSettingsService.getExtraBackupPath();
    }

    public void saveExtraBackupPath(String path) {
        backupSettingsService.saveExtraBackupPath(path);
    }

    public void clearExtraBackupPath() {
        backupSettingsService.clearExtraBackupPath();
    }

    private void copyToExtraBackupPathIfConfigured(Path backupFile) {
        Optional<Path> extraBackupPath = backupSettingsService.getExtraBackupPath();

        if (extraBackupPath.isEmpty()) {
            return;
        }

        try {
            Path extraDir = extraBackupPath.get();
            Files.createDirectories(extraDir);

            Path extraBackupFile = extraDir.resolve(backupFile.getFileName());

            Files.copy(backupFile, extraBackupFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("Failed to create extra backup copy: " + e.getMessage());
        }
    }

    private void deleteOldBackupsIfNeeded() {
        List<Path> backups = listBackups();

        if (backups.size() <= MAX_BACKUPS_TO_KEEP) {
            return;
        }

        List<Path> backupsToDelete = backups.subList(MAX_BACKUPS_TO_KEEP, backups.size());

        for (Path backup : backupsToDelete) {
            try {
                Files.deleteIfExists(backup);
            } catch (IOException e) {
                System.out.println("Failed to delete old backup: " + backup);
            }
        }
    }

    private Path resolveBackupDir(Path dbPath) {
        Path parent = dbPath.getParent();

        if (parent == null) {
            return Path.of("backups");
        }

        return parent.resolve("backups");
    }
}