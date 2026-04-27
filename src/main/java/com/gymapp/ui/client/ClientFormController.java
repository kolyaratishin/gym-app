package com.gymapp.ui.client;

import com.gymapp.app.client.ClientService;
import com.gymapp.domain.client.Client;
import com.gymapp.infrastructure.db.ConnectionFactory;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import com.gymapp.infrastructure.repository.sqlite.SqliteClientRepository;
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

    public ClientFormController() {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();
        SqliteClientRepository clientRepository = new SqliteClientRepository(connectionFactory);
        this.clientService = new ClientService(clientRepository);
    }

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

    public void setOnClientSaved(Runnable onClientSaved) {
        this.onClientSaved = onClientSaved;
    }

    public void setClient(Client client) {
        this.editingClient = client;

        firstNameField.setText(client.getFirstName());
        lastNameField.setText(client.getLastName());
        phoneField.setText(client.getPhone());
        birthDatePicker.setValue(client.getBirthDate());
        notesArea.setText(client.getNotes());
    }

    @FXML
    private void onSave() {
        String validationError = validateForm();
        if (validationError != null) {
            errorLabel.setText(validationError);
            return;
        }

        if (editingClient == null) {
            Client client = new Client();
            client.setFirstName(firstNameField.getText().trim());
            client.setLastName(lastNameField.getText().trim());
            client.setPhone(emptyToNull(phoneField.getText()));
            client.setBirthDate(birthDatePicker.getValue());
            client.setNotes(emptyToNull(notesArea.getText()));
            client.setRegistrationDate(LocalDate.now());
            client.setActive(true);

            clientService.save(client);
        } else {
            editingClient.setFirstName(firstNameField.getText().trim());
            editingClient.setLastName(lastNameField.getText().trim());
            editingClient.setPhone(emptyToNull(phoneField.getText()));
            editingClient.setBirthDate(birthDatePicker.getValue());
            editingClient.setNotes(emptyToNull(notesArea.getText()));

            clientService.update(editingClient);
        }

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
        String firstName = firstNameField.getText() != null ? firstNameField.getText().trim() : "";
        String lastName = lastNameField.getText() != null ? lastNameField.getText().trim() : "";
        String phone = phoneField.getText() != null ? phoneField.getText().trim() : "";

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

    private String emptyToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}