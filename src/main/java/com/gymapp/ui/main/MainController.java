package com.gymapp.ui.main;

import com.gymapp.app.backup.BackupService;
import com.gymapp.app.client.ClientCsvService;
import com.gymapp.app.client.ImportResult;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import com.gymapp.infrastructure.repository.sqlite.SqliteClientRepository;
import com.gymapp.infrastructure.repository.sqlite.SqliteMembershipRepository;
import com.gymapp.infrastructure.repository.sqlite.SqliteMembershipTypeRepository;
import com.gymapp.ui.common.ImportResultController;
import com.gymapp.ui.common.InfoDialogController;
import com.gymapp.ui.common.RestoreBackupController;
import java.io.File;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;
import javafx.stage.Window;

public class MainController {

    private final BackupService backupService = new BackupService();
    private final com.gymapp.infrastructure.db.ConnectionFactory connectionFactory =
            new com.gymapp.infrastructure.db.SqliteConnectionFactory();

    private final com.gymapp.app.client.ClientCsvService clientCsvService =
            new com.gymapp.app.client.ClientCsvService(
                    new SqliteClientRepository(connectionFactory),
                    new SqliteMembershipRepository(connectionFactory),
                    new SqliteMembershipTypeRepository(connectionFactory)
            );

    @FXML
    private StackPane contentPane;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button clientsButton;

    @FXML
    private Button membershipTypesButton;

    @FXML
    private Button createBackupButton;

    @FXML
    private Button restoreBackupButton;

    @FXML
    private Button exportClientsButton;
    @FXML
    private Button importClientsButton;

    @FXML
    public void initialize() {
        loadView("/fxml/dashboard/DashboardView.fxml");
        setActiveNavButton(dashboardButton);
    }

    @FXML
    private void showDashboard() {
        loadView("/fxml/dashboard/DashboardView.fxml");
        setActiveNavButton(dashboardButton);
    }

    @FXML
    private void showClients() {
        loadView("/fxml/client/ClientsView.fxml");
        setActiveNavButton(clientsButton);
    }

    @FXML
    private void showMembershipTypes() {
        loadView("/fxml/membership/MembershipTypesView.fxml");
        setActiveNavButton(membershipTypesButton);
    }

    @FXML
    private void createBackup() {
        try {
            Path backupFile = backupService.createLocalBackup();
            showInfoDialog(
                    "Backup створено",
                    "Файл backup успішно створено:\n" + backupFile
            );
        } catch (Exception e) {
            showInfoDialog(
                    "Помилка backup",
                    "Не вдалося створити backup:\n" + e.getMessage()
            );
        }
    }

    @FXML
    private void restoreBackup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/common/RestoreBackupView.fxml")
            );

            Scene scene = new Scene(loader.load(), 520, 420);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            RestoreBackupController controller = loader.getController();
            controller.setOnRestoreCompleted(() ->
                    showInfoDialog(
                            "Backup відновлено",
                            "Backup успішно відновлено.\nПерезапусти додаток, щоб зміни точно застосувались."
                    )
            );

            Stage stage = new Stage();
            stage.setTitle("Відновлення backup");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open restore backup dialog", e);
        }
    }

    @FXML
    private void exportClients() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Експорт клієнтів у CSV");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV files", "*.csv")
        );
        fileChooser.setInitialFileName("clients-export.csv");

        javafx.stage.Window window = contentPane.getScene() != null ? contentPane.getScene().getWindow() : null;
        java.io.File selectedFile = fileChooser.showSaveDialog(window);

        if (selectedFile == null) {
            return;
        }

        try {
            java.nio.file.Path exportedFile = clientCsvService.exportClients(selectedFile.toPath());
            showInfoDialog(
                    "Експорт завершено",
                    "Клієнтів успішно експортовано у файл:\n" + exportedFile
            );
        } catch (Exception e) {
            showInfoDialog(
                    "Помилка експорту",
                    "Не вдалося експортувати клієнтів:\n" + e.getMessage()
            );
        }
    }

    @FXML
    private void importClients() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Імпорт клієнтів з CSV");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("CSV files", "*.csv")
        );

        Window window = contentPane.getScene() != null ? contentPane.getScene().getWindow() : null;
        File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile == null) {
            return;
        }

        ImportResult result = clientCsvService.importClients(selectedFile.toPath());
        showImportResultDialog(result);

        loadView("/fxml/client/ClientsView.fxml");
        setActiveNavButton(clientsButton);
    }

    private void showImportResultDialog(ImportResult result) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/common/ImportResultView.fxml")
            );

            Scene scene = new Scene(loader.load(), 700, 500);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            ImportResultController controller = loader.getController();
            controller.setResult(result);

            Stage stage = new Stage();
            stage.setTitle("Результат імпорту");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setMinWidth(650);
            stage.setMinHeight(450);
            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open import result dialog", e);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }

    private void showInfoDialog(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/common/InfoDialogView.fxml")
            );

            Scene scene = new Scene(loader.load(), 460, 320);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            InfoDialogController controller = loader.getController();
            controller.setData(title, message);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open info dialog", e);
        }
    }

    private void setActiveNavButton(javafx.scene.control.Button activeButton) {
        dashboardButton.getStyleClass().remove("nav-button-active");
        clientsButton.getStyleClass().remove("nav-button-active");
        membershipTypesButton.getStyleClass().remove("nav-button-active");
        createBackupButton.getStyleClass().remove("nav-button-active");
        restoreBackupButton.getStyleClass().remove("nav-button-active");

        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }
}