package com.gymapp.backup;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.db.SqliteConnectionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Statement;
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
    private final ConnectionFactory connectionFactory;

    private volatile boolean restored;

    public BackupService(
            BackupSettingsService backupSettingsService
    ) {
        this.backupSettingsService = backupSettingsService;
        this.connectionFactory = new SqliteConnectionFactory();
    }

    public Optional<Path> createPreMigrationBackup() {
        Path dbPath = SqliteConnectionFactory.getDbPath();

        /*
         * Перший запуск програми:
         * бази ще немає, тому backup робити нема з чого.
         */
        if (!Files.exists(dbPath)) {
            return Optional.empty();
        }

        Path backupDir = resolveBackupDir(dbPath);

        try {
            Files.createDirectories(backupDir);

            String timestamp =
                    LocalDateTime.now()
                            .format(BACKUP_NAME_FORMAT);

            Path backupFile =
                    backupDir.resolve(
                            "gym-pre-migration-" + timestamp + ".db"
                    );

            /*
             * Тут Files.copy є допустимим:
             *
             * метод викликається на самому початку запуску,
             * до Flyway, Telegram, UI та інших DB-операцій.
             *
             * Тобто gym.db у цей момент не змінюється
             * потоками самого Gym App.
             */
            Files.copy(
                    dbPath,
                    backupFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            copyToExtraBackupPathIfConfigured(backupFile);
            deleteOldBackupsIfNeeded();

            return Optional.of(backupFile);

        } catch (IOException e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.BACKUP_CREATE_LOCAL,
                    "preMigration=true"
                            + ", dbPath=" + dbPath
                            + ", backupDir=" + backupDir,
                    e
            );

            throw new RuntimeException(
                    "Failed to create pre-migration backup",
                    e
            );
        }
    }

    public Path createLocalBackup() {
        Path dbPath = SqliteConnectionFactory.getDbPath();

        if (!Files.exists(dbPath)) {
            throw new RuntimeException(
                    "Database file not found: " + dbPath
            );
        }

        Path backupDir = resolveBackupDir(dbPath);

        try {
            Files.createDirectories(backupDir);

            String timestamp =
                    LocalDateTime.now()
                            .format(BACKUP_NAME_FORMAT);

            Path backupFile =
                    backupDir.resolve(
                            "gym-backup-" + timestamp + ".db"
                    );

            createSqliteSnapshot(backupFile);

            copyToExtraBackupPathIfConfigured(backupFile);
            deleteOldBackupsIfNeeded();

            return backupFile;

        } catch (Exception e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.BACKUP_CREATE_LOCAL,
                    "dbPath="
                            + dbPath
                            + ", backupDir="
                            + backupDir,
                    e
            );

            throw new RuntimeException(
                    "Failed to create local backup",
                    e
            );
        }
    }

    public List<Path> listBackups() {
        Path backupDir =
                resolveBackupDir(
                        SqliteConnectionFactory.getDbPath()
                );

        if (!Files.exists(backupDir)) {
            return List.of();
        }

        try (Stream<Path> stream =
                     Files.list(backupDir)) {

            return stream
                    .filter(Files::isRegularFile)
                    .filter(
                            path ->
                                    path.getFileName()
                                            .toString()
                                            .endsWith(".db")
                    )
                    .sorted(Comparator.reverseOrder())
                    .toList();

        } catch (IOException e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.BACKUP_LIST,
                    "backupDir=" + backupDir,
                    e
            );

            throw new RuntimeException(
                    "Failed to list backups",
                    e
            );
        }
    }

    public void restoreBackup(
            Path backupFile
    ) {
        if (backupFile == null
                || !Files.exists(backupFile)) {

            throw new RuntimeException(
                    "Backup file not found: "
                            + backupFile
            );
        }

        Path dbPath =
                SqliteConnectionFactory.getDbPath();

        try {
            Path parent =
                    dbPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.copy(
                    backupFile,
                    dbPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            /*
             * Після restore застосунок повинен
             * завершити роботу.
             *
             * Flag не дозволяє GymApplication.stop()
             * створити shutdown backup поверх
             * щойно відновленої бази.
             */
            restored = true;

        } catch (IOException e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.BACKUP_RESTORE,
                    "backupFile="
                            + backupFile
                            + ", dbPath="
                            + dbPath,
                    e
            );

            throw new RuntimeException(
                    "Failed to restore backup",
                    e
            );
        }
    }

    public boolean wasRestored() {
        return restored;
    }

    public Optional<Path> getExtraBackupPath() {
        return backupSettingsService
                .getExtraBackupPath();
    }

    public void saveExtraBackupPath(
            String path
    ) {
        backupSettingsService
                .saveExtraBackupPath(path);
    }

    public void clearExtraBackupPath() {
        backupSettingsService
                .clearExtraBackupPath();
    }

    private void createSqliteSnapshot(
            Path backupFile
    ) throws Exception {

        Files.deleteIfExists(backupFile);

        String escapedPath =
                backupFile
                        .toAbsolutePath()
                        .toString()
                        .replace("'", "''");

        String sql =
                "VACUUM INTO '" + escapedPath + "'";

        try (
                Connection connection =
                        connectionFactory.getConnection();

                Statement statement =
                        connection.createStatement()
        ) {
            statement.execute(sql);
        }
    }

    private void copyToExtraBackupPathIfConfigured(
            Path backupFile
    ) {
        Optional<Path> extraBackupPath =
                backupSettingsService
                        .getExtraBackupPath();

        if (extraBackupPath.isEmpty()) {
            return;
        }

        try {
            Path extraDir =
                    extraBackupPath.get();

            Files.createDirectories(extraDir);

            Path extraBackupFile =
                    extraDir.resolve(
                            backupFile.getFileName()
                    );

            Files.copy(
                    backupFile,
                    extraBackupFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (Exception e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.BACKUP_COPY_EXTRA,
                    "backupFile="
                            + backupFile
                            + ", extraBackupPath="
                            + extraBackupPath.orElse(null),
                    e
            );
        }
    }

    private void deleteOldBackupsIfNeeded() {
        List<Path> backups =
                listBackups();

        if (backups.size()
                <= MAX_BACKUPS_TO_KEEP) {

            return;
        }

        List<Path> backupsToDelete =
                backups.subList(
                        MAX_BACKUPS_TO_KEEP,
                        backups.size()
                );

        for (Path backup : backupsToDelete) {
            try {
                Files.deleteIfExists(backup);

            } catch (IOException e) {
                ErrorHandler.logOnly(
                        ErrorLogMessages.BACKUP_DELETE_OLD,
                        "backupFile=" + backup,
                        e
                );
            }
        }
    }

    private Path resolveBackupDir(
            Path dbPath
    ) {
        Path parent =
                dbPath.getParent();

        if (parent == null) {
            return Path.of("backups");
        }

        return parent.resolve("backups");
    }
}