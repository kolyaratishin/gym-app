package com.gymapp.ui.database;

import com.gymapp.backup.BackupService;
import com.gymapp.context.AppContext;
import com.gymapp.ui.common.DialogService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

public class DatabaseController {

    private final BackupService backupService;

    @FXML
    private TextField extraBackupPathField;

    @FXML
    private Label backupStatusLabel;

    public DatabaseController() {
        this.backupService = AppContext.backupService();
    }

    @FXML
    public void initialize() {
        backupService.getExtraBackupPath()
                .ifPresent(path -> extraBackupPathField.setText(path.toString()));
    }

    @FXML
    private void onCreateBackup() {
        try {
            Path backupFile = backupService.createLocalBackup();

            DialogService.showInfo(
                    "Резервна копія",
                    "Резервну копію створено:\n" + backupFile.toAbsolutePath()
            );

            backupStatusLabel.setText("Копію створено: " + backupFile.toAbsolutePath());
        } catch (Exception e) {
            DialogService.showInfo(
                    "Помилка",
                    "Не вдалося створити резервну копію:\n" + e.getMessage()
            );
        }
    }

    @FXML
    private void onChooseExtraBackupPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Виберіть папку для додаткової копії");

        Stage stage = (Stage) extraBackupPathField.getScene().getWindow();
        File selectedDirectory = chooser.showDialog(stage);

        if (selectedDirectory != null) {
            extraBackupPathField.setText(selectedDirectory.toPath().toString());
        }
    }

    @FXML
    private void onSaveExtraBackupPath() {
        backupService.saveExtraBackupPath(extraBackupPathField.getText());

        DialogService.showInfo(
                "Налаштування",
                "Шлях для додаткової копії збережено."
        );
    }

    @FXML
    private void onClearExtraBackupPath() {
        extraBackupPathField.clear();
        backupService.clearExtraBackupPath();

        DialogService.showInfo(
                "Налаштування",
                "Додатковий шлях очищено."
        );
    }

    @FXML
    private void onRestoreBackup() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Виберіть резервну копію для відновлення");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQLite backup (*.db)", "*.db")
        );

        Stage stage = (Stage) extraBackupPathField.getScene().getWindow();
        File selectedFile = chooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return;
        }

        boolean confirmed = DialogService.showConfirm(
                "Підтвердження відновлення",
                "Увага! Поточна база даних буде замінена.\n\nВідновити базу з файлу?\n"
                        + selectedFile.getAbsolutePath()
        );

        if (!confirmed) {
            return;
        }

        try {
            backupService.restoreBackup(selectedFile.toPath());

            DialogService.showInfo(
                    "Відновлення",
                    "Базу даних успішно відновлено.\nРекомендується перезапустити додаток."
            );
        } catch (Exception e) {
            DialogService.showInfo(
                    "Помилка",
                    "Не вдалося відновити базу:\n" + e.getMessage()
            );
        }
    }
}