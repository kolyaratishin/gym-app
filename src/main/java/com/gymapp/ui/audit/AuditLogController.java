package com.gymapp.ui.audit;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.audit.UserErrorMessages;
import com.gymapp.audit.dto.ActivityLogEntry;
import com.gymapp.audit.dto.ErrorLogEntry;
import com.gymapp.audit.service.LogReaderService;
import com.gymapp.ui.common.DialogService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class AuditLogController {

    private final LogReaderService logReaderService = new LogReaderService();

    @FXML
    private DatePicker activityDatePicker;

    @FXML
    private TableView<ActivityLogEntry> activityTable;

    @FXML
    private TableColumn<ActivityLogEntry, String> activityTimeColumn;

    @FXML
    private TableColumn<ActivityLogEntry, String> activityTypeColumn;

    @FXML
    private TableColumn<ActivityLogEntry, String> activityDescriptionColumn;

    @FXML
    private Label activityStatusLabel;

    @FXML
    private DatePicker errorDatePicker;

    @FXML
    private TableView<ErrorLogEntry> errorTable;

    @FXML
    private TableColumn<ErrorLogEntry, String> errorTimeColumn;

    @FXML
    private TableColumn<ErrorLogEntry, String> errorSourceColumn;

    @FXML
    private TableColumn<ErrorLogEntry, String> errorTypeColumn;

    @FXML
    private TableColumn<ErrorLogEntry, String> errorMessageColumn;

    @FXML
    private TextArea errorDetailsArea;

    @FXML
    private Label errorStatusLabel;

    @FXML
    public void initialize() {
        configureActivityTable();
        configureErrorTable();

        activityDatePicker.setValue(LocalDate.now());
        errorDatePicker.setValue(LocalDate.now());

        activityDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> loadActivityLogs());
        errorDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> loadErrorLogs());

        errorTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selectedError) -> {
            if (selectedError == null) {
                errorDetailsArea.clear();
            } else {
                errorDetailsArea.setText(selectedError.getDetails());
            }
        });

        loadActivityLogs();
        loadErrorLogs();
    }

    @FXML
    private void loadActivityLogs() {
        try {
            LocalDate date = activityDatePicker.getValue();
            List<ActivityLogEntry> entries = logReaderService.readActivityLogs(date);

            activityTable.setItems(FXCollections.observableArrayList(entries));
            activityStatusLabel.setText(statusText(entries.size()));
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.AUDIT_LOAD_ACTIVITY,
                    UserErrorMessages.LOGS_LOAD_FAILED,
                    "date=" + activityDatePicker.getValue(),
                    e
            );
        }
    }

    @FXML
    private void loadErrorLogs() {
        try {
            LocalDate date = errorDatePicker.getValue();
            List<ErrorLogEntry> entries = logReaderService.readErrorLogs(date);

            errorTable.setItems(FXCollections.observableArrayList(entries));
            errorDetailsArea.clear();
            errorStatusLabel.setText(statusText(entries.size()));
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.AUDIT_LOAD_ERRORS,
                    UserErrorMessages.LOGS_LOAD_FAILED,
                    "date=" + errorDatePicker.getValue(),
                    e
            );
        }
    }

    @FXML
    private void openLogsFolder() {
        try {
            Path logsDirectory = logReaderService.logsDirectory();
            Files.createDirectories(logsDirectory);

            if (!Desktop.isDesktopSupported()) {
                DialogService.showInfo(
                        "Журнал",
                        "Папка з журналами:\n" + logsDirectory.toAbsolutePath()
                );
                return;
            }

            Desktop.getDesktop().open(logsDirectory.toFile());
        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.AUDIT_OPEN_LOG_FOLDER,
                    UserErrorMessages.LOG_FOLDER_OPEN_FAILED,
                    e
            );
        }
    }

    private void configureActivityTable() {
        activityTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        activityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("eventLabel"));
        activityDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void configureErrorTable() {
        errorTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        errorSourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        errorTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        errorMessageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
    }

    private String statusText(int count) {
        if (count == 0) {
            return "Записів не знайдено";
        }

        return "Записів: " + count;
    }
}
