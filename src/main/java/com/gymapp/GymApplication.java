package com.gymapp;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.audit.GlobalExceptionHandler;
import com.gymapp.backup.BackupService;
import com.gymapp.context.AppContext;
import com.gymapp.db.FlywayMigrator;
import com.gymapp.db.SqliteConnectionFactory;
import com.gymapp.telegram.bot.GymTelegramBot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GymApplication extends Application {

    private final BackupService backupService =
            AppContext.backupService();

    private final GymTelegramBot telegramBot =
            AppContext.telegramBot();

    public static void main(String[] args) {
        GlobalExceptionHandler.install();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            GlobalExceptionHandler.install();

            new FlywayMigrator(
                    SqliteConnectionFactory.getUrl()
            ).migrate();

            createStartupBackupSilently();

            /*
             * Telegram є додатковою інтеграцією.
             *
             * Якщо token відсутній або Telegram не вдалося
             * запустити, Gym App продовжує працювати.
             */
            boolean telegramStarted =
                    startTelegramSilently();

            FXMLLoader fxmlLoader =
                    new FXMLLoader(
                            GymApplication.class.getResource(
                                    "/fxml/main/MainLayout.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            fxmlLoader.load(),
                            1200,
                            700
                    );

            scene.getStylesheets().add(
                    GymApplication.class
                            .getResource("/css/app.css")
                            .toExternalForm()
            );

            stage.setTitle("Gym App");
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setMaximized(true);

            stage.show();

            /*
             * Автоматичні Telegram-повідомлення запускаємо
             * тільки якщо бот реально успішно стартував.
             */
            if (telegramStarted) {
                sendStartupTelegramNotifications();
            }

        } catch (Throwable e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.APPLICATION_START,
                    e
            );

            throw new RuntimeException(e);
        }
    }

    private boolean startTelegramSilently() {
        /*
         * telegramBot == null означає:
         * token не налаштований.
         *
         * Це нормальний сценарій.
         */
        if (telegramBot == null) {
            System.out.println(
                    "Telegram bot is not configured"
            );

            return false;
        }

        try {
            boolean started =
                    telegramBot.start();

            if (!started) {
                System.err.println(
                        "Telegram bot could not be started. "
                                + "Gym App will continue without Telegram."
                );
            }

            return started;

        } catch (Exception e) {
            /*
             * Будь-яка проблема Telegram
             * не повинна ламати Gym App.
             */
            ErrorHandler.logOnly(
                    ErrorLogMessages.APPLICATION_START,
                    e
            );

            System.err.println(
                    "Telegram startup failed. "
                            + "Gym App will continue without Telegram."
            );

            return false;
        }
    }

    private void sendStartupTelegramNotifications() {
        Thread thread =
                new Thread(
                        () -> {
                            try {
                                AppContext.membershipService()
                                        .expireOutdatedMemberships();

                                AppContext.telegramNotificationService()
                                        .sendExpiringMembershipNotifications();

                                AppContext.telegramNotificationService()
                                        .sendExpiredMembershipNotifications();

                            } catch (Exception e) {
                                ErrorHandler.logOnly(
                                        ErrorLogMessages.APPLICATION_START,
                                        e
                                );
                            }
                        },
                        "telegram-startup-notifications"
                );

        thread.setDaemon(true);
        thread.start();
    }

    private void createStartupBackupSilently() {
        try {
            backupService.createLocalBackup();

            System.out.println(
                    "Startup backup created"
            );

        } catch (Exception e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.APPLICATION_STARTUP_BACKUP,
                    e
            );
        }
    }

    private void createShutdownBackupSilently() {
        try {
            backupService.createLocalBackup();

            System.out.println(
                    "Shutdown backup created"
            );

        } catch (Exception e) {
            ErrorHandler.logOnly(
                    ErrorLogMessages.APPLICATION_SHUTDOWN_BACKUP,
                    e
            );
        }
    }

    @Override
    public void stop() {
        /*
         * Якщо Telegram взагалі не налаштований,
         * telegramBot буде null.
         */
        if (telegramBot != null) {
            telegramBot.stop();
        }

        createShutdownBackupSilently();
    }
}