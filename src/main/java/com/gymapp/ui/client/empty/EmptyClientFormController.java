package com.gymapp.ui.client.empty;

import com.gymapp.client.service.ClientService;
import com.gymapp.context.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmptyClientFormController {

    // Service
    private final ClientService clientService;

    // State
    private Runnable onClientSaved;

    // FXML
    @FXML
    private TextField clientNumberField;

    @FXML
    private Label errorLabel;

    public EmptyClientFormController() {
        this.clientService = AppContext.clientService();
    }

    // Public API

    public void setOnClientSaved(Runnable onClientSaved) {
        this.onClientSaved = onClientSaved;
    }

    // Actions

    @FXML
    private void onSave() {
        String validationError = validate();

        if (validationError != null) {
            showError(validationError);
            return;
        }

        Integer clientNumber = Integer.parseInt(getTrimmedValue());

        clientService.createEmptyClient(clientNumber);

        notifySaved();
        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    // Validation

    private String validate() {
        String value = getTrimmedValue();

        if (value.isEmpty()) {
            return "Введіть номер клієнта";
        }

        if (!isInteger(value)) {
            return "Номер клієнта має бути числом";
        }

        int parsed = Integer.parseInt(value);

        if (parsed <= 0) {
            return "Номер клієнта має бути більше 0";
        }

        if (clientService.existsByClientNumber(parsed)) {
            return "Клієнт з таким номером вже існує";
        }

        return null;
    }

    // Helpers

    private String getTrimmedValue() {
        return clientNumberField.getText() == null
                ? ""
                : clientNumberField.getText().trim();
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void notifySaved() {
        if (onClientSaved != null) {
            onClientSaved.run();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) clientNumberField.getScene().getWindow();
        stage.close();
    }
}