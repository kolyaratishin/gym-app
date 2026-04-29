package com.gymapp.ui.common;

import com.gymapp.backup.BackupService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;

public class RestoreBackupController {

    private final BackupService backupService = new BackupService();

    private Path selectedBackup;
    private Runnable onRestoreCompleted;

    @FXML
    private ListView<Path> backupListView;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        List<Path> backups = backupService.listBackups();
        backupListView.setItems(FXCollections.observableArrayList(backups));

        backupListView.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFileName().toString());
            }
        });
    }

    public void setOnRestoreCompleted(Runnable onRestoreCompleted) {
        this.onRestoreCompleted = onRestoreCompleted;
    }

    @FXML
    private void onRestore() {
        selectedBackup = backupListView.getSelectionModel().getSelectedItem();

        if (selectedBackup == null) {
            errorLabel.setText("Оберіть backup-файл");
            return;
        }

        try {
            backupService.restoreBackup(selectedBackup);

            if (onRestoreCompleted != null) {
                onRestoreCompleted.run();
            }

            close();
        } catch (Exception e) {
            errorLabel.setText("Помилка відновлення: " + e.getMessage());
        }
    }

    @FXML
    private void onClose() {
        close();
    }

    public Path getSelectedBackup() {
        return selectedBackup;
    }

    private void close() {
        Stage stage = (Stage) backupListView.getScene().getWindow();
        stage.close();
    }
}