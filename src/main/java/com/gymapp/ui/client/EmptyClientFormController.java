package com.gymapp.ui.client;

import com.gymapp.app.client.ClientService;
import com.gymapp.infrastructure.db.ConnectionFactory;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import com.gymapp.infrastructure.repository.sqlite.SqliteClientRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmptyClientFormController {

    private final ClientService clientService;
    private Runnable onClientSaved;

    public EmptyClientFormController() {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();
        this.clientService = new ClientService(new SqliteClientRepository(connectionFactory));
    }

    @FXML
    private TextField clientNumberField;

    @FXML
    private Label errorLabel;

    public void setOnClientSaved(Runnable onClientSaved) {
        this.onClientSaved = onClientSaved;
    }

    @FXML
    private void onSave() {
        String validationError = validateForm();
        if (validationError != null) {
            errorLabel.setText(validationError);
            return;
        }

        Integer clientNumber = Integer.parseInt(clientNumberField.getText().trim());

        clientService.createEmptyClient(clientNumber);

        if (onClientSaved != null) {
            onClientSaved.run();
        }

        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private String validateForm() {
        String value = clientNumberField.getText();

        if (value == null || value.trim().isEmpty()) {
            return "Введіть номер клієнта";
        }

        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed <= 0) {
                return "Номер клієнта має бути більше 0";
            }

            if (clientService.existsByClientNumber(parsed)) {
                return "Клієнт з таким номером вже існує";
            }
        } catch (NumberFormatException e) {
            return "Номер клієнта має бути числом";
        }

        return null;
    }

    private void closeWindow() {
        Stage stage = (Stage) clientNumberField.getScene().getWindow();
        stage.close();
    }
}