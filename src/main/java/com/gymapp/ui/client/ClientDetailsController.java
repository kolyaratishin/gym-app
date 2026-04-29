package com.gymapp.ui.client;

import com.gymapp.visit.service.VisitService;
import com.gymapp.client.db.Client;
import com.gymapp.membership.db.*;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.visit.db.VisitRepository;
import com.gymapp.infrastructure.db.ConnectionFactory;
import com.gymapp.infrastructure.db.SqliteConnectionFactory;
import com.gymapp.visit.db.SqliteVisitRepository;
import com.gymapp.infrastructure.util.GymAppUtils;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;
import com.gymapp.membership.service.MembershipTypeService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Optional;

public class ClientDetailsController {

    private final MembershipRepository membershipRepository;
    private final MembershipTypeService membershipTypeService;
    private final VisitService visitService;
    private final VisitRepository visitRepository;

    public ClientDetailsController() {
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();

        this.membershipRepository = new SqliteMembershipRepository(connectionFactory);
        this.membershipTypeService = new MembershipTypeService(
                new SqliteMembershipTypeRepository(connectionFactory)
        );
        this.visitService = new VisitService(
                new SqliteVisitRepository(connectionFactory),
                new SqliteMembershipRepository(connectionFactory),
                new MembershipTypeService(new SqliteMembershipTypeRepository(connectionFactory))
        );
        this.visitRepository = new SqliteVisitRepository(connectionFactory);
    }

    @FXML
    private Label idValueLabel;

    @FXML
    private Label firstNameValueLabel;

    @FXML
    private Label lastNameValueLabel;

    @FXML
    private Label phoneValueLabel;

    @FXML
    private Label birthDateValueLabel;

    @FXML
    private Label notesValueLabel;

    @FXML
    private Label registrationDateValueLabel;

    @FXML
    private Label activeValueLabel;

    @FXML
    private Label membershipStatusValueLabel;

    @FXML
    private Label membershipTypeValueLabel;

    @FXML
    private Label membershipPolicyValueLabel;

    @FXML
    private Label membershipStartDateValueLabel;

    @FXML
    private Label membershipEndDateValueLabel;

    @FXML
    private Label membershipRemainingVisitsValueLabel;

    @FXML
    private Label membershipPriceValueLabel;

    @FXML
    private Label membershipDateStateValueLabel;

    @FXML
    private Button manageMembershipButton;

    @FXML
    private Label visitedTodayIndicatorLabel;

    @FXML
    private Label membershipAlertIndicatorLabel;

    @FXML
    private Label clientNumberValueLabel;

    private Client client;


    private Runnable onClientUpdated;

    public void setOnClientUpdated(Runnable onClientUpdated) {
        this.onClientUpdated = onClientUpdated;
    }

    public void setClient(Client client) {
        this.client = client;
        idValueLabel.setText(client.getClientNumber() != null ? String.valueOf(client.getClientNumber()) : "-");
        firstNameValueLabel.setText(nullToDash(client.getFirstName()));
        lastNameValueLabel.setText(nullToDash(client.getLastName()));
        phoneValueLabel.setText(nullToDash(client.getPhone()));
        birthDateValueLabel.setText(client.getBirthDate() != null ? client.getBirthDate().toString() : "-");
        notesValueLabel.setText(nullToDash(client.getNotes()));
        registrationDateValueLabel.setText(client.getRegistrationDate() != null ? client.getRegistrationDate().toString() : "-");
        activeValueLabel.setText(
                membershipRepository.findActiveByClientId(client.getId()).isPresent() ? "Так" : "Ні"
        );

        loadMembershipInfo(client.getId());

        updateVisitedTodayIndicator(client.getId());
    }

    private void loadMembershipInfo(Long clientId) {
        Optional<Membership> membershipOptional = membershipRepository.findActiveByClientId(clientId);

        if (membershipOptional.isEmpty()) {
            membershipStatusValueLabel.setText("Немає активного абонемента");
            membershipTypeValueLabel.setText("-");
            membershipPolicyValueLabel.setText("-");
            membershipPriceValueLabel.setText("-");
            membershipDateStateValueLabel.setText("-");
            membershipStartDateValueLabel.setText("-");
            membershipEndDateValueLabel.setText("-");
            membershipRemainingVisitsValueLabel.setText("-");
            manageMembershipButton.setText("Призначити абонемент");
            applyBadgeStyle(membershipAlertIndicatorLabel, "✖ Немає абонемента", "status-pill-danger");
            return;
        }

        Membership membership = membershipOptional.get();

        membershipStatusValueLabel.setText(formatMembershipStatus(membership.getStatus()));
        membershipStartDateValueLabel.setText(
                membership.getStartDate() != null ? membership.getStartDate().toString() : "-"
        );
        membershipEndDateValueLabel.setText(
                membership.getEndDate() != null ? membership.getEndDate().toString() : "-"
        );
        membershipRemainingVisitsValueLabel.setText(
                membership.getRemainingVisits() != null ? membership.getRemainingVisits().toString() : "-"
        );

        Optional<MembershipType> membershipTypeOptional = membershipTypeService.findById(membership.getMembershipTypeId());

        if (membershipTypeOptional.isPresent()) {
            MembershipType membershipType = membershipTypeOptional.get();

            membershipTypeValueLabel.setText(nullToDash(membershipType.getName()));
            membershipPolicyValueLabel.setText(
                    membershipType.getVisitPolicy() != null ? formatVisitPolicy(membershipType.getVisitPolicy()) : "-"
            );
            membershipPriceValueLabel.setText(
                    membershipType.getPrice() != null ? membershipType.getPrice().toPlainString() : "-"
            );
            membershipDateStateValueLabel.setText(resolveDateState(membership));
            updateMembershipAlertIndicator(membership, membershipTypeOptional.orElse(null));
        } else {
            membershipTypeValueLabel.setText("-");
            membershipPolicyValueLabel.setText("-");
            membershipPriceValueLabel.setText("-");
            membershipDateStateValueLabel.setText("-");
        }

        manageMembershipButton.setText("Замінити абонемент");
    }

    private void updateMembershipAlertIndicator(Membership membership, MembershipType membershipType) {
        if (membership == null || membershipType == null) {
            applyBadgeStyle(membershipAlertIndicatorLabel, "✖ Немає даних", "status-pill-danger");
            return;
        }

        if (membership.getStatus() == MembershipStatus.EXPIRED) {
            applyBadgeStyle(membershipAlertIndicatorLabel, "✖ Прострочений", "status-pill-danger");
            return;
        }

        if (membershipType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            Integer remaining = membership.getRemainingVisits();

            if (remaining == null || remaining <= 0) {
                applyBadgeStyle(membershipAlertIndicatorLabel, "✖ Прострочений", "status-pill-danger");
            } else if (remaining == 1) {
                applyBadgeStyle(membershipAlertIndicatorLabel, "✖ 1 тренування", "status-pill-danger");
            } else if (remaining <= 2) {
                applyBadgeStyle(membershipAlertIndicatorLabel, "⚠ Мало тренувань", "status-pill-warning");
            } else {
                applyBadgeStyle(membershipAlertIndicatorLabel, "✔ Абонемент ок", "status-pill-success");
            }
            return;
        }

        if (membership.getEndDate() == null) {
            applyBadgeStyle(membershipAlertIndicatorLabel, "✔ Абонемент ок", "status-pill-success");
            return;
        }

        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(),
                membership.getEndDate()
        );

        if (daysLeft < 0) {
            applyBadgeStyle(membershipAlertIndicatorLabel, "✖ Прострочений", "status-pill-danger");
        } else if (daysLeft <= 1) {
            applyBadgeStyle(membershipAlertIndicatorLabel, "✖ Закінчується", "status-pill-danger");
        } else if (daysLeft <= 3) {
            applyBadgeStyle(membershipAlertIndicatorLabel, "⚠ Скоро закінчиться", "status-pill-warning");
        } else {
            applyBadgeStyle(membershipAlertIndicatorLabel, "✔ Абонемент ок", "status-pill-success");
        }
    }

    private String formatMembershipStatus(MembershipStatus status) {
        return switch (status) {
            case ACTIVE -> "Активний";
            case EXPIRED -> "Прострочений";
            case FROZEN -> "Заморожений";
            case CANCELLED -> "Скасований";
        };
    }

    private String formatVisitPolicy(VisitPolicy visitPolicy) {
        return switch (visitPolicy) {
            case UNLIMITED -> "Безлімітний";
            case LIMITED_BY_VISITS -> "За кількістю відвідувань";
            case LIMITED_BY_TIME -> "За часом";
        };
    }

    private String resolveDateState(Membership membership) {
        if (membership.getEndDate() == null) {
            return "Без обмеження";
        }

        if (membership.getEndDate().isBefore(java.time.LocalDate.now())) {
            return "Прострочений";
        }

        if (membership.getEndDate().isEqual(java.time.LocalDate.now())) {
            return "Останній день";
        }

        return "Активний";
    }

    @FXML
    private void onManageMembership() {
        if (client == null) {
            return;
        }

        try {
            FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/client/ClientMembershipFormView.fxml")
            );

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            ClientMembershipFormController controller = loader.getController();
            controller.setClient(client);
            controller.setOnMembershipSaved(() -> {
                loadMembershipInfo(client.getId());
                updateVisitedTodayIndicator(client.getId());

                if (onClientUpdated != null) {
                    onClientUpdated.run();
                }
            });

            Stage stage = new Stage();
            GymAppUtils.applyResponsiveStageSize(stage, 0.72, 0.92);
            stage.setTitle("Керування абонементом");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setMinWidth(560);
            stage.setMinHeight(300);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open membership form", e);
        }
    }

    @FXML
    private void onRegisterVisit() {
        if (client == null) {
            return;
        }

        boolean confirmed = showConfirmDialog(
                "Підтвердження",
                "Підтвердити тренування для " + client.getFirstName() + " " + client.getLastName() + "?"
        );

        if (!confirmed) {
            return;
        }

        String resultMessage = visitService.registerVisit(client.getId());
        showInfoDialog("Реєстрація відвідування", resultMessage);

        loadMembershipInfo(client.getId());
        updateVisitedTodayIndicator(client.getId());
    }

    private void updateVisitedTodayIndicator(Long clientId) {
        boolean visitedToday = visitRepository.findByClientId(clientId).stream()
                .anyMatch(visit -> visit.getVisitTime() != null
                        && visit.getVisitTime().toLocalDate().isEqual(java.time.LocalDate.now()));

        if (visitedToday) {
            applyBadgeStyle(visitedTodayIndicatorLabel, "✔ Сьогодні був", "status-pill-success");
        } else {
            applyBadgeStyle(visitedTodayIndicatorLabel, "✖ Сьогодні не був", "status-pill-danger");
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) idValueLabel.getScene().getWindow();
        stage.close();
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void applyBadgeStyle(Label label, String text, String pillType) {
        label.setText(text);
        label.getStyleClass().setAll("status-pill", pillType);
    }

    private boolean showConfirmDialog(String title, String message) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/common/ConfirmDialogView.fxml")
            );

            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 420, 200);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            com.gymapp.ui.common.ConfirmDialogController controller = loader.getController();
            controller.setData(title, message);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(title);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

            return controller.isConfirmed();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open confirm dialog", e);
        }
    }

    private void showInfoDialog(String title, String message) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/common/InfoDialogView.fxml")
            );

            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 420, 180);
            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            com.gymapp.ui.common.InfoDialogController controller = loader.getController();
            controller.setData(title, message);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(title);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            throw new RuntimeException("Failed to open info dialog", e);
        }
    }
}