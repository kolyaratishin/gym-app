package com.gymapp.ui.common;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class InfoDialogController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label messageLabel;

    public void setData(String title, String message) {
        titleLabel.setText(title);
        messageLabel.setText(message);
    }

    @FXML
    private void onOk() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}