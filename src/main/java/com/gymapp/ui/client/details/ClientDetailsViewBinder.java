package com.gymapp.ui.client.details;

import com.gymapp.client.db.Client;
import javafx.scene.control.Label;

import java.time.LocalDate;

public class ClientDetailsViewBinder {

    private final Label idValueLabel;
    private final Label firstNameValueLabel;
    private final Label lastNameValueLabel;
    private final Label phoneValueLabel;
    private final Label birthDateValueLabel;
    private final Label notesValueLabel;
    private final Label registrationDateValueLabel;

    public ClientDetailsViewBinder(
            Label idValueLabel,
            Label firstNameValueLabel,
            Label lastNameValueLabel,
            Label phoneValueLabel,
            Label birthDateValueLabel,
            Label notesValueLabel,
            Label registrationDateValueLabel
    ) {
        this.idValueLabel = idValueLabel;
        this.firstNameValueLabel = firstNameValueLabel;
        this.lastNameValueLabel = lastNameValueLabel;
        this.phoneValueLabel = phoneValueLabel;
        this.birthDateValueLabel = birthDateValueLabel;
        this.notesValueLabel = notesValueLabel;
        this.registrationDateValueLabel = registrationDateValueLabel;
    }

    public void showClient(Client client) {
        String clientNumber = formatInteger(client.getClientNumber());

        idValueLabel.setText(clientNumber);
        firstNameValueLabel.setText(nullToDash(client.getFirstName()));
        lastNameValueLabel.setText(nullToDash(client.getLastName()));
        phoneValueLabel.setText(nullToDash(client.getPhone()));
        birthDateValueLabel.setText(formatDate(client.getBirthDate()));
        notesValueLabel.setText(nullToDash(client.getNotes()));
        registrationDateValueLabel.setText(formatDate(client.getRegistrationDate()));
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "-";
    }

    private String formatInteger(Integer value) {
        return value != null ? value.toString() : "-";
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}