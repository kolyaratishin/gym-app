package com.gymapp;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.audit.GlobalExceptionHandler;
import com.gymapp.backup.BackupService;
import com.gymapp.context.AppContext;
import com.gymapp.db.FlywayMigrator;
import com.gymapp.db.SqliteConnectionFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class GymApplication extends Application {

    private final BackupService backupService = AppContext.backupService();

    public static void main(String[] args) {
        GlobalExceptionHandler.install();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            GlobalExceptionHandler.install();
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
            ErrorHandler.logOnly(ErrorLogMessages.APPLICATION_START, e);
            throw new RuntimeException(e);
        }
    }

    private void createStartupBackupSilently() {
        try {
            backupService.createLocalBackup();
            System.out.println("Startup backup created");
        } catch (Exception e) {
            ErrorHandler.logOnly(ErrorLogMessages.APPLICATION_STARTUP_BACKUP, e);
        }
    }

    private void createShutdownBackupSilently() {
        try {
            backupService.createLocalBackup();
            System.out.println("Shutdown backup created");
        } catch (Exception e) {
            ErrorHandler.logOnly(ErrorLogMessages.APPLICATION_SHUTDOWN_BACKUP, e);
        }
    }
}