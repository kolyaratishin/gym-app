package com.gymapp.ui.client.mebmership;

import com.gymapp.client.db.Client;
import com.gymapp.context.AppContext;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.service.MembershipService;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.util.DatePickerUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.regex.Pattern;

public class ClientMembershipFormController {

    private static final Pattern TEEN_PATTERN =
            Pattern.compile("(13\\s*[-–—]\\s*17)|(13\\s*до\\s*17)", Pattern.CASE_INSENSITIVE);

    private final MembershipService membershipService;
    private final MembershipTypeService membershipTypeService;

    private Client client;
    private Runnable onMembershipSaved;

    private ClientMembershipPreviewBinder previewBinder;
    private ClientMembershipCurrentViewBinder currentViewBinder;
    private ClientMembershipFormValidator validator;
    private ClientMembershipFormSaver formSaver;
    private ClientMembershipManualFieldsBinder manualFieldsBinder;

    @FXML
    private Label clientInfoLabel;

    @FXML
    private ComboBox<MembershipType> membershipTypeBox;

    @FXML
    private Label currentMembershipLabel;

    @FXML
    private Label replacementInfoLabel;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private CheckBox manualModeCheckBox;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField remainingVisitsField;

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
        this.membershipService = AppContext.membershipService();
        this.membershipTypeService = AppContext.membershipTypeService();
    }

    @FXML
    public void initialize() {
        initializeBinders();
        initializeFormDefaults();
        initializeMembershipTypeBox();
        initializeListeners();

        updateManualFieldsState();
        clearPreview();
    }

    private void initializeBinders() {
        this.previewBinder = new ClientMembershipPreviewBinder(
                previewPolicyLabel,
                previewEndDateLabel,
                previewRemainingVisitsLabel,
                previewStatusLabel
        );

        this.currentViewBinder = new ClientMembershipCurrentViewBinder(
                currentMembershipLabel,
                replacementInfoLabel
        );

        this.validator = new ClientMembershipFormValidator();
        this.formSaver = new ClientMembershipFormSaver(membershipService);

        this.manualFieldsBinder = new ClientMembershipManualFieldsBinder(
                endDatePicker,
                remainingVisitsField
        );
    }

    private void initializeFormDefaults() {
        DatePickerUtils.configure(startDatePicker);
        DatePickerUtils.configure(endDatePicker);

        startDatePicker.setValue(LocalDate.now());
    }

    private void initializeMembershipTypeBox() {
        membershipTypeBox.setCellFactory(param -> new MembershipTypeListCell());
        membershipTypeBox.setButtonCell(new MembershipTypeListCell());
    }

    private void initializeListeners() {
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
    }

    public void setClient(Client client) {
        this.client = client;

        clientInfoLabel.setText(
                "Клієнт: " + client.getFirstName() + " " + client.getLastName() + " (ID: " + client.getId() + ")"
        );

        loadMembershipTypes();
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
            showError(validationError);
            return;
        }

        saveMembership();
        notifyMembershipSaved();
        closeWindow();
    }

    @FXML
    private void onClose() {
        closeWindow();
    }

    private void loadMembershipTypes() {
        List<MembershipType> activeTypes = membershipTypeService.findActive();
        List<MembershipType> allowedTypes = filterMembershipTypesByAge(activeTypes);

        membershipTypeBox.setItems(FXCollections.observableArrayList(allowedTypes));
    }

    private List<MembershipType> filterMembershipTypesByAge(List<MembershipType> types) {
        if (client == null || client.getBirthDate() == null) {
            return types.stream()
                    .filter(type -> !isTeenMembership(type))
                    .toList();
        }

        int age = Period.between(client.getBirthDate(), LocalDate.now()).getYears();

        return types.stream()
                .filter(type -> isMembershipAllowedForAge(type, age))
                .toList();
    }

    private boolean isMembershipAllowedForAge(MembershipType type, int age) {
        if (!isTeenMembership(type)) {
            return true;
        }

        return age >= 0 && age <= 17;
    }

    private boolean isTeenMembership(MembershipType type) {
        if (type == null || type.getName() == null) {
            return false;
        }

        return TEEN_PATTERN.matcher(type.getName()).find();
    }

    private void loadCurrentMembership() {
        currentViewBinder.showCurrentMembership(
                membershipService.findActiveByClientId(client.getId())
        );
    }

    private String validateForm() {
        return validator.validate(
                client != null ? client.getId() : null,
                getSelectedType(),
                startDatePicker.getValue(),
                isManualMode(),
                endDatePicker.getValue(),
                remainingVisitsField.getText()
        );
    }

    private void saveMembership() {
        formSaver.save(
                client.getId(),
                getSelectedType(),
                startDatePicker.getValue(),
                isManualMode(),
                endDatePicker.getValue(),
                parseRemainingVisitsOrNull()
        );
    }

    private void notifyMembershipSaved() {
        if (onMembershipSaved != null) {
            onMembershipSaved.run();
        }
    }

    private void updateManualFieldsState() {
        manualFieldsBinder.update(isManualMode(), getSelectedType());
    }

    private void updatePreview() {
        previewBinder.update(
                getSelectedType(),
                startDatePicker.getValue(),
                isManualMode(),
                endDatePicker.getValue(),
                remainingVisitsField.getText()
        );
    }

    private void clearPreview() {
        previewBinder.clear();
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private MembershipType getSelectedType() {
        return membershipTypeBox.getValue();
    }

    private boolean isManualMode() {
        return manualModeCheckBox.isSelected();
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

    private void closeWindow() {
        Stage stage = (Stage) clientInfoLabel.getScene().getWindow();
        stage.close();
    }

    private static class MembershipTypeListCell extends ListCell<MembershipType> {

        @Override
        protected void updateItem(MembershipType item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : item.getName());
        }
    }
}