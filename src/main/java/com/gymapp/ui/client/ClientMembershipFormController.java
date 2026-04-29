package com.gymapp.ui.client;

import com.gymapp.membership.service.MembershipService;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.client.db.Client;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.MembershipStatus;
import com.gymapp.membership.db.domain.VisitPolicy;
import com.gymapp.infrastructure.db.ConnectionFactory;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import com.gymapp.membership.db.SqliteMembershipRepository;
import com.gymapp.membership.db.SqliteMembershipTypeRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ClientMembershipFormController {

    private final MembershipService membershipService;
    private final MembershipTypeService membershipTypeService;

    private Client client;
    private Runnable onMembershipSaved;

    @FXML
    private Label clientInfoLabel;

    @FXML
    private ComboBox<MembershipType> membershipTypeBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private Label currentMembershipLabel;

    @FXML
    private Label replacementInfoLabel;

    @FXML
    private Label previewPolicyLabel;

    @FXML
    private Label previewEndDateLabel;

    @FXML
    private Label previewRemainingVisitsLabel;

    @FXML
    private Label previewStatusLabel;

    @FXML
    private CheckBox manualModeCheckBox;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField remainingVisitsField;

    @FXML
    private Label errorLabel;

    public ClientMembershipFormController() {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();

        this.membershipService = new MembershipService(
                new SqliteMembershipRepository(connectionFactory)
        );
        this.membershipTypeService = new MembershipTypeService(
                new SqliteMembershipTypeRepository(connectionFactory)
        );
    }

    @FXML
    public void initialize() {
        startDatePicker.setValue(LocalDate.now());
        loadMembershipTypes();

        membershipTypeBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MembershipType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        membershipTypeBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(MembershipType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        membershipTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateManualFieldsState();
            updatePreview();
        });
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updatePreview());

        manualModeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateManualFieldsState();
            updatePreview();
        });

        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updatePreview());
        remainingVisitsField.textProperty().addListener((observable, oldValue, newValue) -> updatePreview());

        updateManualFieldsState();

        clearPreview();
    }

    private void updateManualFieldsState() {
        boolean manualMode = manualModeCheckBox.isSelected();
        MembershipType selectedType = membershipTypeBox.getValue();

        endDatePicker.setDisable(!manualMode);
        remainingVisitsField.setDisable(true);

        if (!manualMode) {
            endDatePicker.setValue(null);
            remainingVisitsField.clear();
            return;
        }

        if (selectedType != null && selectedType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            remainingVisitsField.setDisable(false);
        }
    }

    public void setClient(Client client) {
        this.client = client;
        clientInfoLabel.setText(
                "Клієнт: " + client.getFirstName() + " " + client.getLastName() + " (ID: " + client.getId() + ")"
        );
        loadCurrentMembership();
        updatePreview();
    }

    public void setOnMembershipSaved(Runnable onMembershipSaved) {
        this.onMembershipSaved = onMembershipSaved;
    }

    @FXML
    private void onSave() {
        String validationError = validateForm();
        if (validationError != null) {
            errorLabel.setText(validationError);
            return;
        }

        MembershipType selectedType = membershipTypeBox.getValue();
        LocalDate startDate = startDatePicker.getValue();

        boolean hasActiveMembership = membershipService.findActiveByClientId(client.getId()).isPresent();

        if (manualModeCheckBox.isSelected()) {
            LocalDate endDate = endDatePicker.getValue();
            Integer remainingVisits = parseRemainingVisitsOrNull();

            if (hasActiveMembership) {
                membershipService.replaceWithManualMembership(
                        client.getId(),
                        selectedType,
                        startDate,
                        endDate,
                        remainingVisits
                );
            } else {
                membershipService.createManualMembership(
                        client.getId(),
                        selectedType,
                        startDate,
                        endDate,
                        remainingVisits
                );
            }
        } else {
            if (hasActiveMembership) {
                membershipService.replaceMembership(client.getId(), selectedType, startDate);
            } else {
                membershipService.createMembership(client.getId(), selectedType, startDate);
            }
        }

        if (onMembershipSaved != null) {
            onMembershipSaved.run();
        }

        closeWindow();
    }

    @FXML
    private void onClose() {
        closeWindow();
    }

    private void loadMembershipTypes() {
        List<MembershipType> activeTypes = membershipTypeService.findActive();
        membershipTypeBox.setItems(FXCollections.observableArrayList(activeTypes));
    }

    private void loadCurrentMembership() {
        Optional<Membership> currentMembership = membershipService.findActiveByClientId(client.getId());

        if (currentMembership.isEmpty()) {
            currentMembershipLabel.setText("Немає активного абонемента");
            replacementInfoLabel.setText("Буде створено новий абонемент");
            return;
        }

        Membership membership = currentMembership.get();
        String text = "Статус: " + formatMembershipStatus(membership.getStatus())
                + ", початок: " + (membership.getStartDate() != null ? membership.getStartDate() : "-")
                + ", завершення: " + (membership.getEndDate() != null ? membership.getEndDate() : "-")
                + ", залишок: " + (membership.getRemainingVisits() != null ? membership.getRemainingVisits() : "-");

        currentMembershipLabel.setText(text);
        replacementInfoLabel.setText("Поточний активний абонемент буде замінено новим");
    }

    private String formatMembershipStatus(MembershipStatus status) {
        return switch (status) {
            case ACTIVE -> "Активний";
            case EXPIRED -> "Прострочений";
            case FROZEN -> "Заморожений";
            case CANCELLED -> "Скасований";
        };
    }

    private void updatePreview() {
        MembershipType selectedType = membershipTypeBox.getValue();
        LocalDate startDate = startDatePicker.getValue();

        if (selectedType == null || startDate == null) {
            clearPreview();
            return;
        }

        VisitPolicy visitPolicy = selectedType.getVisitPolicy();

        previewPolicyLabel.setText(formatVisitPolicy(visitPolicy));
        previewStatusLabel.setText("Активний");

        if (manualModeCheckBox.isSelected()) {
            previewEndDateLabel.setText(
                    endDatePicker.getValue() != null ? endDatePicker.getValue().toString() : "-"
            );

            if (visitPolicy == VisitPolicy.LIMITED_BY_VISITS) {
                previewRemainingVisitsLabel.setText(
                        valueOrEmpty(remainingVisitsField.getText()).isBlank()
                                ? "-"
                                : remainingVisitsField.getText().trim()
                );
            } else {
                previewRemainingVisitsLabel.setText("-");
            }

            return;
        }

        switch (visitPolicy) {
            case LIMITED_BY_VISITS -> {
                if (selectedType.getDurationDays() != null) {
                    previewEndDateLabel.setText(startDate.plusDays(selectedType.getDurationDays()).toString());
                } else {
                    previewEndDateLabel.setText("-");
                }

                previewRemainingVisitsLabel.setText(
                        selectedType.getVisitLimit() != null ? selectedType.getVisitLimit().toString() : "-"
                );
            }
            case LIMITED_BY_TIME, UNLIMITED -> {
                if (selectedType.getDurationDays() != null) {
                    previewEndDateLabel.setText(startDate.plusDays(selectedType.getDurationDays()).toString());
                } else {
                    previewEndDateLabel.setText("-");
                }

                previewRemainingVisitsLabel.setText("-");
            }
        }
    }

    private void clearPreview() {
        previewPolicyLabel.setText("-");
        previewEndDateLabel.setText("-");
        previewRemainingVisitsLabel.setText("-");
        previewStatusLabel.setText("-");
    }

    private String validateForm() {
        if (client == null) {
            return "Клієнт не вибраний";
        }

        MembershipType selectedType = membershipTypeBox.getValue();

        if (selectedType == null) {
            return "Потрібно вибрати тип абонемента";
        }

        if (startDatePicker.getValue() == null) {
            return "Потрібно вибрати дату початку";
        }

        if (!manualModeCheckBox.isSelected()) {
            return null;
        }

        if (endDatePicker.getValue() == null) {
            return "Потрібно вказати дату завершення";
        }

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            return "Дата завершення не може бути раніше дати початку";
        }

        if (selectedType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            String remainingVisits = valueOrEmpty(remainingVisitsField.getText()).trim();

            if (remainingVisits.isEmpty()) {
                return "Потрібно вказати залишок тренувань";
            }

            try {
                int parsed = Integer.parseInt(remainingVisits);
                if (parsed < 0) {
                    return "Залишок тренувань не може бути від'ємним";
                }
            } catch (NumberFormatException e) {
                return "Залишок тренувань має бути числом";
            }
        }

        return null;
    }

    private Integer parseRemainingVisitsOrNull() {
        String value = valueOrEmpty(remainingVisitsField.getText()).trim();

        if (value.isEmpty()) {
            return null;
        }

        return Integer.parseInt(value);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatVisitPolicy(VisitPolicy visitPolicy) {
        return switch (visitPolicy) {
            case UNLIMITED -> "Безлімітний";
            case LIMITED_BY_VISITS -> "За кількістю відвідувань";
            case LIMITED_BY_TIME -> "За часом";
        };
    }

    private void closeWindow() {
        Stage stage = (Stage) clientInfoLabel.getScene().getWindow();
        stage.close();
    }
}