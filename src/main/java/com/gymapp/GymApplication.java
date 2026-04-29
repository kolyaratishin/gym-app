package com.gymapp;

import com.gymapp.backup.BackupService;
import com.gymapp.infrastructure.db.FlywayMigrator;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GymApplication extends Application {

    private final BackupService backupService = new BackupService();

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> logError("Uncaught exception", throwable));
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            new FlywayMigrator(SqliteConnectionFactory.getUrl()).migrate();

            createStartupBackupSilently();

            FXMLLoader fxmlLoader = new FXMLLoader(
                    GymApplication.class.getResource("/fxml/main/MainLayout.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
            scene.getStylesheets().add(
                    GymApplication.class.getResource("/css/app.css").toExternalForm()
            );

            stage.setTitle("Gym App");
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setMaximized(true);

            stage.setOnCloseRequest(event -> createShutdownBackupSilently());

            stage.show();
        } catch (Throwable e) {
            logError("Failed to start application", e);
            throw new RuntimeException(e);
        }
    }

    private void createStartupBackupSilently() {
        try {
            backupService.createLocalBackup();
            System.out.println("Startup backup created");
        } catch (Exception e) {
            System.out.println("Startup backup failed: " + e.getMessage());
        }
    }

    private void createShutdownBackupSilently() {
        try {
            backupService.createLocalBackup();
            System.out.println("Shutdown backup created");
        } catch (Exception e) {
            System.out.println("Shutdown backup failed: " + e.getMessage());
        }
    }

    private static void logError(String message, Throwable throwable) {
        try {
            Path logDir = Paths.get(System.getProperty("user.home"), "GymApp", "logs");
            Files.createDirectories(logDir);

            Path logFile = logDir.resolve("startup-error.log");

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println(message);
            throwable.printStackTrace(pw);
            pw.println("--------------------------------------------------");

            Files.writeString(
                    logFile,
                    sw.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
        }
    }
}