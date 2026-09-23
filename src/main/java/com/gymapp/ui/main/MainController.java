package com.gymapp.ui.main;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.audit.UserErrorMessages;
import com.gymapp.client.dto.ImportResult;
import com.gymapp.client.service.ClientCsvService;
import com.gymapp.context.AppContext;
import com.gymapp.ui.common.DialogService;
import com.gymapp.ui.common.ImportResultController;
import com.gymapp.ui.common.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

import java.io.File;

public class MainController {

    private final ClientCsvService clientCsvService =
            AppContext.clientCsvService();

    @FXML
    private StackPane contentPane;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button clientsButton;

    @FXML
    private Button membershipTypesButton;

    @FXML
    private Button telegramButton;

    @FXML
    private Button databaseButton;

    @FXML
    private Button auditButton;

    @FXML
    private Button settingsButton;

    @FXML
    public void initialize() {
        configureTelegramFeature();

        loadView("/fxml/dashboard/DashboardView.fxml");
        setActiveNavButton(dashboardButton);
    }

    private void configureTelegramFeature() {
        boolean telegramEnabled =
                AppContext.isTelegramEnabled();

        telegramButton.setVisible(telegramEnabled);
        telegramButton.setManaged(telegramEnabled);
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
    private void showTelegram() {
        if (!AppContext.isTelegramEnabled()) {
            return;
        }

        loadView("/fxml/telegram/TelegramView.fxml");
        setActiveNavButton(telegramButton);
    }

    @FXML
    private void showDatabase() {
        loadView("/fxml/database/DatabaseView.fxml");
        setActiveNavButton(databaseButton);
    }

    @FXML
    private void showAudit() {
        loadView("/fxml/audit/AuditLogView.fxml");
        setActiveNavButton(auditButton);
    }

    @FXML
    private void exportClients() {
        javafx.stage.FileChooser fileChooser =
                new javafx.stage.FileChooser();

        fileChooser.setTitle("Експорт клієнтів у CSV");

        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter(
                        "CSV files",
                        "*.csv"
                )
        );

        fileChooser.setInitialFileName("clients-export.csv");

        Window window =
                contentPane.getScene() != null
                        ? contentPane.getScene().getWindow()
                        : null;

        File selectedFile =
                fileChooser.showSaveDialog(window);

        if (selectedFile == null) {
            return;
        }

        try {
            java.nio.file.Path exportedFile =
                    clientCsvService.exportClients(
                            selectedFile.toPath()
                    );

            DialogService.showInfo(
                    "Експорт завершено",
                    "Клієнтів успішно експортовано у файл:\n"
                            + exportedFile
            );

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.MAIN_EXPORT_CLIENTS,
                    UserErrorMessages.EXPORT_CLIENTS_FAILED,
                    "file=" + selectedFile.getAbsolutePath(),
                    e
            );
        }
    }

    @FXML
    private void importClients() {
        javafx.stage.FileChooser fileChooser =
                new javafx.stage.FileChooser();

        fileChooser.setTitle("Імпорт клієнтів з CSV");

        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter(
                        "CSV files",
                        "*.csv"
                )
        );

        Window window =
                contentPane.getScene() != null
                        ? contentPane.getScene().getWindow()
                        : null;

        File selectedFile =
                fileChooser.showOpenDialog(window);

        if (selectedFile == null) {
            return;
        }

        try {
            ImportResult result =
                    clientCsvService.importClients(
                            selectedFile.toPath()
                    );

            showImportResultDialog(result);

            loadView("/fxml/client/ClientsView.fxml");
            setActiveNavButton(clientsButton);

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.MAIN_IMPORT_CLIENTS,
                    UserErrorMessages.IMPORT_CLIENTS_FAILED,
                    "file=" + selectedFile.getAbsolutePath(),
                    e
            );
        }
    }

    @FXML
    private void showSettings() {
        loadView("/fxml/settings/SettingsView.fxml");
        setActiveNavButton(settingsButton);
    }

    private void showImportResultDialog(
            ImportResult result
    ) {
        try {
            ViewLoader.showModalAndReturnController(
                    "/fxml/common/ImportResultView.fxml",
                    "Результат імпорту",
                    0.55,
                    0.55,
                    (ImportResultController controller) ->
                            controller.setResult(result)
            );

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.MAIN_SHOW_IMPORT_RESULT,
                    UserErrorMessages.VIEW_LOAD_FAILED,
                    e
            );
        }
    }

    private void loadView(String fxmlPath) {
        try {
            contentPane.getChildren().setAll(
                    ViewLoader.loadContent(fxmlPath)
            );

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.MAIN_LOAD_VIEW,
                    UserErrorMessages.VIEW_LOAD_FAILED,
                    "fxmlPath=" + fxmlPath,
                    e
            );
        }
    }

    private void setActiveNavButton(
            Button activeButton
    ) {
        dashboardButton
                .getStyleClass()
                .remove("nav-button-active");

        clientsButton
                .getStyleClass()
                .remove("nav-button-active");

        membershipTypesButton
                .getStyleClass()
                .remove("nav-button-active");

        telegramButton
                .getStyleClass()
                .remove("nav-button-active");

        databaseButton
                .getStyleClass()
                .remove("nav-button-active");

        auditButton
                .getStyleClass()
                .remove("nav-button-active");

        settingsButton
                .getStyleClass()
                .remove("nav-button-active");

        if (activeButton != null
                && !activeButton
                .getStyleClass()
                .contains("nav-button-active")) {

            activeButton
                    .getStyleClass()
                    .add("nav-button-active");
        }
    }
}