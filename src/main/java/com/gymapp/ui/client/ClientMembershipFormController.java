package com.gymapp.ui.client;

import com.gymapp.app.membership.MembershipService;
import com.gymapp.app.membership.MembershipTypeService;
import com.gymapp.domain.client.Client;
import com.gymapp.domain.membership.Membership;
import com.gymapp.domain.membership.MembershipType;
import com.gymapp.domain.membership.MembershipStatus;
import com.gymapp.domain.membership.VisitPolicy;
import com.gymapp.infrastructure.db.ConnectionFactory;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import com.gymapp.infrastructure.repository.sqlite.SqliteMembershipRepository;
import com.gymapp.infrastructure.repository.sqlite.SqliteMembershipTypeRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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

        membershipTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> updatePreview());
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> updatePreview());

        clearPreview();
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

        if (membershipService.findActiveByClientId(client.getId()).isPresent()) {
            membershipService.replaceMembership(client.getId(), selectedType, startDate);
        } else {
            membershipService.createMembership(client.getId(), selectedType, startDate);
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

        if (membershipTypeBox.getValue() == null) {
            return "Потрібно вибрати тип абонемента";
        }

        if (startDatePicker.getValue() == null) {
            return "Потрібно вибрати дату початку";
        }

        return null;
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