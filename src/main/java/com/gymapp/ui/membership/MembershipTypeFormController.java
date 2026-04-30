package com.gymapp.ui.membership;

import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.db.SqliteConnectionFactory;
import com.gymapp.membership.db.SqliteMembershipTypeRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import javafx.util.StringConverter;

public class MembershipTypeFormController {

    private final MembershipTypeService membershipTypeService;
    private Runnable onMembershipTypeSaved;
    private MembershipType editingMembershipType;

    public MembershipTypeFormController() {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();
        SqliteMembershipTypeRepository repository = new SqliteMembershipTypeRepository(connectionFactory);
        this.membershipTypeService = new MembershipTypeService(repository);
    }

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<VisitPolicy> visitPolicyBox;

    @FXML
    private TextField durationDaysField;

    @FXML
    private TextField visitLimitField;

    @FXML
    private TextField priceField;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        visitPolicyBox.setItems(FXCollections.observableArrayList(VisitPolicy.values()));
        visitPolicyBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(VisitPolicy visitPolicy) {
                if (visitPolicy == null) {
                    return "";
                }

                return switch (visitPolicy) {
                    case UNLIMITED -> "Безлімітний";
                    case LIMITED_BY_VISITS -> "За кількістю відвідувань";
                    case LIMITED_BY_TIME -> "За часом";
                };
            }

            @Override
            public VisitPolicy fromString(String string) {
                return null;
            }
        });

        visitPolicyBox.setValue(VisitPolicy.UNLIMITED);
        activeCheckBox.setSelected(true);

        visitPolicyBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFieldsState());
        updateFieldsState();
    }

    public void setOnMembershipTypeSaved(Runnable onMembershipTypeSaved) {
        this.onMembershipTypeSaved = onMembershipTypeSaved;
    }

    public void setMembershipType(MembershipType membershipType) {
        this.editingMembershipType = membershipType;

        nameField.setText(membershipType.getName());
        visitPolicyBox.setValue(membershipType.getVisitPolicy());
        durationDaysField.setText(membershipType.getDurationDays() != null ? membershipType.getDurationDays().toString() : "");
        visitLimitField.setText(membershipType.getVisitLimit() != null ? membershipType.getVisitLimit().toString() : "");
        priceField.setText(membershipType.getPrice() != null ? membershipType.getPrice().toPlainString() : "");
        activeCheckBox.setSelected(membershipType.isActive());

        updateFieldsState();
    }

    @FXML
    private void onSave() {
        String validationError = validateForm();
        if (validationError != null) {
            errorLabel.setText(validationError);
            return;
        }

        if (editingMembershipType == null) {
            MembershipType membershipType = new MembershipType();
            fillMembershipTypeFromForm(membershipType);
            membershipTypeService.save(membershipType);
        } else {
            fillMembershipTypeFromForm(editingMembershipType);
            membershipTypeService.update(editingMembershipType);
        }

        if (onMembershipTypeSaved != null) {
            onMembershipTypeSaved.run();
        }

        closeWindow();
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void fillMembershipTypeFromForm(MembershipType membershipType) {
        VisitPolicy visitPolicy = visitPolicyBox.getValue();

        membershipType.setName(nameField.getText().trim());
        membershipType.setVisitPolicy(visitPolicy);
        membershipType.setPrice(new BigDecimal(priceField.getText().trim()));
        membershipType.setActive(activeCheckBox.isSelected());

        switch (visitPolicy) {
            case UNLIMITED, LIMITED_BY_TIME -> {
                membershipType.setDurationDays(parseIntegerOrNull(durationDaysField.getText()));
                membershipType.setVisitLimit(null);
            }
            case LIMITED_BY_VISITS -> {
                membershipType.setDurationDays(parseIntegerOrNull(durationDaysField.getText()));
                membershipType.setVisitLimit(parseIntegerOrNull(visitLimitField.getText()));
            }
        }
    }

    private String validateForm() {
        String name = valueOrEmpty(nameField.getText()).trim();
        String durationDays = valueOrEmpty(durationDaysField.getText()).trim();
        String visitLimit = valueOrEmpty(visitLimitField.getText()).trim();
        String price = valueOrEmpty(priceField.getText()).trim();
        VisitPolicy visitPolicy = visitPolicyBox.getValue();

        if (name.isEmpty()) {
            return "Назва є обов'язковою";
        }

        if (visitPolicy == null) {
            return "Потрібно вибрати політику відвідувань";
        }

        if (visitPolicy == VisitPolicy.UNLIMITED || visitPolicy == VisitPolicy.LIMITED_BY_TIME) {
            if (durationDays.isEmpty()) {
                return "Потрібно вказати тривалість у днях";
            }

            try {
                int parsed = Integer.parseInt(durationDays);
                if (parsed <= 0) {
                    return "Тривалість має бути більше 0";
                }
            } catch (NumberFormatException e) {
                return "Тривалість має бути цілим числом";
            }
        }

        if (visitPolicy == VisitPolicy.LIMITED_BY_VISITS) {
            if (durationDays.isEmpty()) {
                return "Потрібно вказати тривалість у днях";
            }

            try {
                int parsed = Integer.parseInt(durationDays);
                if (parsed <= 0) {
                    return "Тривалість має бути більше 0";
                }
            } catch (NumberFormatException e) {
                return "Тривалість має бути цілим числом";
            }

            if (visitLimit.isEmpty()) {
                return "Потрібно вказати ліміт відвідувань";
            }

            try {
                int parsed = Integer.parseInt(visitLimit);
                if (parsed <= 0) {
                    return "Ліміт відвідувань має бути більше 0";
                }
            } catch (NumberFormatException e) {
                return "Ліміт відвідувань має бути цілим числом";
            }
        }

        if (price.isEmpty()) {
            return "Ціна є обов'язковою";
        }

        try {
            BigDecimal parsedPrice = new BigDecimal(price);
            if (parsedPrice.compareTo(BigDecimal.ZERO) < 0) {
                return "Ціна не може бути від'ємною";
            }
        } catch (NumberFormatException e) {
            return "Некоректний формат ціни";
        }

        return null;
    }

    private void updateFieldsState() {
        VisitPolicy visitPolicy = visitPolicyBox.getValue();

        if (visitPolicy == null) {
            durationDaysField.setDisable(false);
            visitLimitField.setDisable(false);
            return;
        }

        switch (visitPolicy) {
            case UNLIMITED, LIMITED_BY_TIME -> {
                durationDaysField.setDisable(false);
                visitLimitField.clear();
                visitLimitField.setDisable(true);
            }
            case LIMITED_BY_VISITS -> {
                durationDaysField.setDisable(false);
                visitLimitField.setDisable(false);
            }
        }
    }

    private Integer parseIntegerOrNull(String value) {
        String trimmed = valueOrEmpty(value).trim();
        return trimmed.isEmpty() ? null : Integer.parseInt(trimmed);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}