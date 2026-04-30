package com.gymapp.ui.client.details;

import com.gymapp.client.db.Client;
import com.gymapp.context.AppContext;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.ui.client.mebmership.ClientMembershipFormController;
import com.gymapp.ui.common.ConfirmDialogController;
import com.gymapp.ui.common.InfoDialogController;
import com.gymapp.ui.common.ViewLoader;
import com.gymapp.visit.db.VisitRepository;
import com.gymapp.visit.service.VisitService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;

public class ClientDetailsController {

    private final MembershipRepository membershipRepository;
    private final MembershipTypeService membershipTypeService;
    private final VisitService visitService;
    private final VisitRepository visitRepository;

    private ClientDetailsViewBinder clientDetailsViewBinder;
    private ClientMembershipViewBinder membershipViewBinder;

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

    private Client client;
    private Runnable onClientUpdated;

    public ClientDetailsController() {
        this.membershipRepository = AppContext.membershipRepository();
        this.membershipTypeService = AppContext.membershipTypeService();
        this.visitService = AppContext.visitService();
        this.visitRepository = AppContext.visitRepository();
    }

    @FXML
    private void initialize() {
        this.clientDetailsViewBinder = new ClientDetailsViewBinder(
                idValueLabel,
                firstNameValueLabel,
                lastNameValueLabel,
                phoneValueLabel,
                birthDateValueLabel,
                notesValueLabel,
                registrationDateValueLabel
        );
        this.membershipViewBinder = new ClientMembershipViewBinder(
                membershipTypeService,
                membershipStatusValueLabel,
                membershipTypeValueLabel,
                membershipPolicyValueLabel,
                membershipStartDateValueLabel,
                membershipEndDateValueLabel,
                membershipRemainingVisitsValueLabel,
                membershipPriceValueLabel,
                membershipDateStateValueLabel,
                membershipAlertIndicatorLabel,
                manageMembershipButton
        );
    }

    public void setOnClientUpdated(Runnable onClientUpdated) {
        this.onClientUpdated = onClientUpdated;
    }

    public void setClient(Client client) {
        this.client = client;
        clientDetailsViewBinder.showClient(client);
        refreshClientState();
    }

    private void refreshClientState() {
        if (client == null) {
            return;
        }

        loadMembershipInfo(client.getId());
        updateVisitedTodayIndicator(client.getId());
    }

    private void loadMembershipInfo(Long clientId) {
        Optional<Membership> membershipOptional = membershipRepository.findActiveByClientId(clientId);

        activeValueLabel.setText(membershipOptional.isPresent() ? "Так" : "Ні");
        membershipViewBinder.showMembership(membershipOptional);
    }

    @FXML
    private void onManageMembership() {
        if (client == null) {
            return;
        }

        Stage stage = ViewLoader.openWindow(
                "/fxml/client/ClientMembershipFormView.fxml",
                "Керування абонементом",
                0.72,
                0.92,
                (ClientMembershipFormController controller) -> {
                    controller.setClient(client);
                    controller.setOnMembershipSaved(() -> {
                        refreshClientState();

                        if (onClientUpdated != null) {
                            onClientUpdated.run();
                        }
                    });
                }
        );

        stage.setMaximized(true);
        stage.setMinWidth(560);
        stage.setMinHeight(300);
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

        refreshClientState();
    }

    private void updateVisitedTodayIndicator(Long clientId) {
        boolean visitedToday = visitRepository.findByClientId(clientId).stream()
                .anyMatch(visit -> visit.getVisitTime() != null
                        && visit.getVisitTime().toLocalDate().isEqual(LocalDate.now()));

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

    private void applyBadgeStyle(Label label, String text, String pillType) {
        label.setText(text);
        label.getStyleClass().setAll("status-pill", pillType);
    }

    private boolean showConfirmDialog(String title, String message) {
        ConfirmDialogController controller =
                ViewLoader.showModalAndReturnController(
                        "/fxml/common/ConfirmDialogView.fxml",
                        title,
                        0.35,
                        0.3,
                        c -> c.setData(title, message)
                );

        return controller.isConfirmed();
    }

    private void showInfoDialog(String title, String message) {
        ViewLoader.showModalAndReturnController(
                "/fxml/common/InfoDialogView.fxml",
                title,
                0.35,
                0.3,
                (InfoDialogController controller) ->
                        controller.setData(title, message)
        );
    }
}