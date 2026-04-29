package com.gymapp.ui.common;

import com.gymapp.client.dto.ImportResult;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class ImportResultController {

    @FXML
    private Label importedLabel;

    @FXML
    private Label skippedLabel;

    @FXML
    private ListView<String> errorsListView;

    public void setResult(ImportResult result) {
        importedLabel.setText("Клієнтів: " + result.getImported());
        importedLabel.getStyleClass().setAll("status-pill", "status-pill-success");

        skippedLabel.setText("Пропущено: " + result.getSkipped());
        skippedLabel.getStyleClass().setAll(
                "status-pill",
                result.getSkipped() > 0 ? "status-pill-warning" : "status-pill-success"
        );

        if (result.hasErrors()) {
            errorsListView.setItems(FXCollections.observableArrayList(result.getErrors()));
        } else {
            errorsListView.setItems(FXCollections.observableArrayList(
                    "Помилок немає",
                    "Абонементів імпортовано: " + result.getMembershipsImported()
            ));
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) importedLabel.getScene().getWindow();
        stage.close();
    }
}