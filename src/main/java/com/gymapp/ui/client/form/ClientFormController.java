package com.gymapp.ui.client.form;

import com.gymapp.client.db.Client;
import com.gymapp.client.service.ClientService;
import com.gymapp.context.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ClientFormController {

    private final ClientService clientService;

    private Runnable onClientSaved;
    private Client editingClient;

    @FXML
    private TextField clientNumberField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private TextArea notesArea;

    @FXML
    private Label errorLabel;

    public ClientFormController() {
        this.clientService = AppContext.clientService();
    }

    public void setOnClientSaved(Runnable onClientSaved) {
        this.onClientSaved = onClientSaved;
    }

    public void setClient(Client client) {
        this.editingClient = client;

        clientNumberField.setText(formatInteger(client.getClientNumber()));
        firstNameField.setText(nullToEmpty(client.getFirstName()));
        lastNameField.setText(nullToEmpty(client.getLastName()));
        phoneField.setText(nullToEmpty(client.getPhone()));
        birthDatePicker.setValue(client.getBirthDate());
        notesArea.setText(nullToEmpty(client.getNotes()));
    }

    @FXML
    private void onSave() {
        String validationError = validateForm();

        if (validationError != null) {
            showError(validationError);
            return;
        }

        if (isEditMode()) {
            updateClient();
        } else {
            createClient();
        }

        notifyClientSaved();
        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private boolean isEditMode() {
        return editingClient != null;
    }

    private void createClient() {
        Client client = new Client();

        fillClientFromForm(client);
        client.setRegistrationDate(LocalDate.now());
        client.setActive(true);

        clientService.save(client);
    }

    private void updateClient() {
        fillClientFromForm(editingClient);
        clientService.update(editingClient);
    }

    private void fillClientFromForm(Client client) {
        client.setClientNumber(Integer.valueOf(getTrimmedText(clientNumberField)));
        client.setFirstName(getTrimmedText(firstNameField));
        client.setLastName(getTrimmedText(lastNameField));
        client.setPhone(emptyToNull(phoneField.getText()));
        client.setBirthDate(birthDatePicker.getValue());
        client.setNotes(emptyToNull(notesArea.getText()));
    }

    private String validateForm() {
        String clientNumber = getTrimmedText(clientNumberField);
        String firstName = getTrimmedText(firstNameField);
        String lastName = getTrimmedText(lastNameField);
        String phone = getTrimmedText(phoneField);

        if (clientNumber.isEmpty()) {
            return "Номер клієнта є обов'язковим";
        }

        if (!isInteger(clientNumber)) {
            return "Номер клієнта має бути числом";
        }

        if (clientService.existsByClientNumber(Integer.valueOf(clientNumber))) {
            return "Клієнт з таким номером вже існує";
        }

        if (firstName.isEmpty()) {
            return "Ім'я є обов'язковим";
        }

        if (lastName.isEmpty()) {
            return "Прізвище є обов'язковим";
        }

        if (!phone.isEmpty() && !phone.matches("^[+0-9()\\-\\s]{7,20}$")) {
            return "Некоректний формат телефону";
        }

        return null;
    }

    private void notifyClientSaved() {
        if (onClientSaved != null) {
            onClientSaved.run();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private String getTrimmedText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatInteger(Integer value) {
        return value != null ? value.toString() : "";
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}