package com.gymapp.ui.client.details;

import com.gymapp.audit.ErrorHandler;
import com.gymapp.audit.ErrorLogMessages;
import com.gymapp.audit.UserErrorMessages;
import com.gymapp.client.db.Client;
import com.gymapp.context.AppContext;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;
import com.gymapp.membership.service.MembershipService;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.telegram.service.TelegramMessagingService;
import com.gymapp.ui.client.history.ClientVisitHistoryController;
import com.gymapp.ui.client.mebmership.ClientMembershipFormController;
import com.gymapp.ui.common.DialogService;
import com.gymapp.ui.common.ViewLoader;
import com.gymapp.ui.telegram.TelegramMessageController;
import com.gymapp.visit.db.VisitRepository;
import com.gymapp.visit.service.VisitService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ClientDetailsController {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final MembershipRepository membershipRepository;
    private final MembershipService membershipService;
    private final MembershipTypeService membershipTypeService;
    private final VisitService visitService;
    private final VisitRepository visitRepository;
    private final TelegramMessagingService telegramMessagingService;

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
    private Button registerVisitButton;

    @FXML
    private Button freezeMembershipButton;

    @FXML
    private Label membershipPausedAtTitleLabel;

    @FXML
    private Label membershipPausedAtValueLabel;

    @FXML
    private Label visitedTodayIndicatorLabel;

    @FXML
    private Label membershipAlertIndicatorLabel;

    @FXML
    private Label telegramStatusLabel;

    @FXML
    private Button sendTelegramButton;

    private Client client;
    private Runnable onClientUpdated;

    public ClientDetailsController() {
        this.membershipRepository =
                AppContext.membershipRepository();

        this.membershipService =
                AppContext.membershipService();

        this.membershipTypeService =
                AppContext.membershipTypeService();

        this.visitService =
                AppContext.visitService();

        this.visitRepository =
                AppContext.visitRepository();

        this.telegramMessagingService =
                AppContext.telegramMessagingService();
    }

    @FXML
    private void initialize() {
        this.clientDetailsViewBinder =
                new ClientDetailsViewBinder(
                        idValueLabel,
                        firstNameValueLabel,
                        lastNameValueLabel,
                        phoneValueLabel,
                        birthDateValueLabel,
                        notesValueLabel,
                        registrationDateValueLabel
                );

        this.membershipViewBinder =
                new ClientMembershipViewBinder(
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

    public void setOnClientUpdated(
            Runnable onClientUpdated
    ) {
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
        updateTelegramState();
    }

    private void loadMembershipInfo(Long clientId) {
        Optional<Membership> membershipOptional =
                membershipService.findCurrentByClientId(
                        clientId
                );

        activeValueLabel.setText(
                membershipOptional.isPresent()
                        ? "Так"
                        : "Ні"
        );

        membershipViewBinder.showMembership(
                membershipOptional
        );

        updateMembershipActions(
                membershipOptional
        );
    }

    private void updateMembershipActions(
            Optional<Membership> membershipOptional
    ) {
        if (membershipOptional.isEmpty()) {
            hideFreezeButton();
            registerVisitButton.setDisable(true);
            hidePausedAt();
            return;
        }

        Membership membership =
                membershipOptional.get();

        MembershipStatus status =
                membership.getStatus();

        if (status == MembershipStatus.FROZEN) {
            freezeMembershipButton.setText(
                    "▶ Відновити"
            );

            setFreezeButtonStyle(
                    "membership-resume-button"
            );

            freezeMembershipButton.setVisible(true);
            freezeMembershipButton.setManaged(true);

            registerVisitButton.setDisable(true);

            showPausedAt(membership);

            return;
        }

        hidePausedAt();

        if (status == MembershipStatus.ACTIVE) {
            registerVisitButton.setDisable(false);

            boolean canFreeze =
                    membership.getEndDate() != null;

            freezeMembershipButton.setText(
                    "❄ Заморозити"
            );

            setFreezeButtonStyle(
                    "membership-freeze-button"
            );

            freezeMembershipButton.setVisible(
                    canFreeze
            );

            freezeMembershipButton.setManaged(
                    canFreeze
            );

            return;
        }

        hideFreezeButton();
        registerVisitButton.setDisable(true);
    }

    private void setFreezeButtonStyle(String styleClass) {
        freezeMembershipButton
                .getStyleClass()
                .setAll(
                        "button",
                        styleClass
                );
    }

    private void showPausedAt(
            Membership membership
    ) {
        if (membership.getPausedAt() == null) {
            hidePausedAt();
            return;
        }

        membershipPausedAtValueLabel.setText(
                membership.getPausedAt()
                        .toLocalDate()
                        .format(DATE_FORMATTER)
        );

        membershipPausedAtTitleLabel.setVisible(true);
        membershipPausedAtTitleLabel.setManaged(true);

        membershipPausedAtValueLabel.setVisible(true);
        membershipPausedAtValueLabel.setManaged(true);
    }

    private void hidePausedAt() {
        membershipPausedAtTitleLabel.setVisible(false);
        membershipPausedAtTitleLabel.setManaged(false);

        membershipPausedAtValueLabel.setVisible(false);
        membershipPausedAtValueLabel.setManaged(false);

        membershipPausedAtValueLabel.setText("");
    }

    private void hideFreezeButton() {
        freezeMembershipButton.setVisible(false);
        freezeMembershipButton.setManaged(false);
    }

    @FXML
    private void onToggleMembershipFreeze() {
        if (client == null) {
            return;
        }

        Optional<Membership> membershipOptional =
                membershipService.findCurrentByClientId(
                        client.getId()
                );

        if (membershipOptional.isEmpty()) {
            return;
        }

        Membership membership =
                membershipOptional.get();

        if (membership.getStatus()
                == MembershipStatus.ACTIVE) {

            freezeMembership(membership);
            return;
        }

        if (membership.getStatus()
                == MembershipStatus.FROZEN) {

            resumeMembership(membership);
        }
    }

    private void freezeMembership(
            Membership membership
    ) {
        boolean confirmed =
                DialogService.showConfirm(
                        "Заморозити абонемент",
                        """
                        Заморозити абонемент цього клієнта?

                        Під час заморозки реєстрація відвідувань буде недоступна.

                        Термін дії абонемента буде продовжено на кількість днів заморозки.
                        """
                );

        if (!confirmed) {
            return;
        }

        try {
            membershipService.freezeMembership(
                    membership.getId()
            );

            refreshClientState();
            notifyClientUpdated();

            DialogService.showInfo(
                    "Абонемент заморожено",
                    """
                    Абонемент успішно заморожено.

                    Відновити його можна у будь-який момент у деталях клієнта.
                    """
            );

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENT_DETAILS_MANAGE_MEMBERSHIP,
                    UserErrorMessages.MEMBERSHIP_MANAGE_OPEN_FAILED,
                    buildClientErrorDetails(),
                    e
            );
        }
    }

    private void resumeMembership(
            Membership membership
    ) {
        boolean confirmed =
                DialogService.showConfirm(
                        "Відновити абонемент",
                        """
                        Відновити абонемент цього клієнта?

                        Дата завершення буде автоматично продовжена на тривалість заморозки.
                        """
                );

        if (!confirmed) {
            return;
        }

        try {
            Membership resumedMembership =
                    membershipService.resumeMembership(
                            membership.getId()
                    );

            refreshClientState();
            notifyClientUpdated();

            String newEndDate =
                    resumedMembership.getEndDate() != null
                            ? resumedMembership
                              .getEndDate()
                              .format(DATE_FORMATTER)
                            : "-";

            DialogService.showInfo(
                    "Абонемент відновлено",
                    "Абонемент успішно відновлено.\n\n"
                            + "Нова дата завершення: "
                            + newEndDate
            );

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENT_DETAILS_MANAGE_MEMBERSHIP,
                    UserErrorMessages.MEMBERSHIP_MANAGE_OPEN_FAILED,
                    buildClientErrorDetails(),
                    e
            );
        }
    }

    private void notifyClientUpdated() {
        if (onClientUpdated != null) {
            onClientUpdated.run();
        }
    }

    private void updateTelegramState() {
        if (!AppContext.isTelegramEnabled()) {
            applyBadgeStyle(
                    telegramStatusLabel,
                    "● Telegram вимкнено",
                    "status-pill-neutral"
            );

            sendTelegramButton.setDisable(true);
            return;
        }

        boolean connected =
                telegramMessagingService
                        .isTelegramConnected(
                                client.getId()
                        );

        if (connected) {
            applyBadgeStyle(
                    telegramStatusLabel,
                    "● Підключено",
                    "status-pill-success"
            );

            sendTelegramButton.setDisable(false);

        } else {
            applyBadgeStyle(
                    telegramStatusLabel,
                    "● Не підключено",
                    "status-pill-neutral"
            );

            sendTelegramButton.setDisable(true);
        }
    }

    @FXML
    private void onSendTelegramMessage() {
        if (client == null) {
            return;
        }

        ViewLoader.openWindow(
                "/fxml/telegram/TelegramMessageView.fxml",
                "Telegram повідомлення",
                0.4,
                0.5,
                (TelegramMessageController controller) ->
                        controller.setClient(client)
        );
    }

    @FXML
    private void onManageMembership() {
        if (client == null) {
            return;
        }

        try {
            Stage stage =
                    ViewLoader.openWindow(
                            "/fxml/client/ClientMembershipFormView.fxml",
                            "Керування абонементом",
                            0.72,
                            0.92,
                            (ClientMembershipFormController controller) -> {
                                controller.setClient(client);

                                controller.setOnMembershipSaved(() -> {
                                    refreshClientState();

                                    notifyClientUpdated();
                                });
                            }
                    );

            stage.setMaximized(true);
            stage.setMinWidth(560);
            stage.setMinHeight(300);

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENT_DETAILS_MANAGE_MEMBERSHIP,
                    UserErrorMessages.MEMBERSHIP_MANAGE_OPEN_FAILED,
                    buildClientErrorDetails(),
                    e
            );
        }
    }

    @FXML
    private void onRegisterVisit() {
        if (client == null) {
            return;
        }

        /*
         * UI protection.
         *
         * VisitService все одно повинен залишатися
         * основним місцем бізнес-перевірки.
         */
        Optional<Membership> membership =
                membershipService.findCurrentByClientId(
                        client.getId()
                );

        if (membership.isEmpty()
                || membership.get().getStatus()
                != MembershipStatus.ACTIVE) {

            DialogService.showInfo(
                    "Відвідування недоступне",
                    "Клієнт не має активного абонемента."
            );

            refreshClientState();
            return;
        }

        boolean confirmed =
                DialogService.showConfirm(
                        "Підтвердження",
                        "Підтвердити тренування для "
                                + client.getFirstName()
                                + " "
                                + client.getLastName()
                                + "?"
                );

        if (!confirmed) {
            return;
        }

        try {
            String resultMessage =
                    visitService.registerVisit(
                            client.getId()
                    );

            DialogService.showInfo(
                    "Реєстрація відвідування",
                    resultMessage
            );

            refreshClientState();

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENT_DETAILS_REGISTER_VISIT,
                    UserErrorMessages.VISIT_REGISTER_FAILED,
                    buildClientErrorDetails(),
                    e
            );
        }
    }

    private void updateVisitedTodayIndicator(
            Long clientId
    ) {
        try {
            boolean visitedToday =
                    visitRepository
                            .findByClientId(clientId)
                            .stream()
                            .anyMatch(
                                    visit ->
                                            visit.getVisitTime() != null
                                                    && visit.getVisitTime()
                                                    .toLocalDate()
                                                    .isEqual(
                                                            LocalDate.now()
                                                    )
                            );

            if (visitedToday) {
                applyBadgeStyle(
                        visitedTodayIndicatorLabel,
                        "✔ Сьогодні був",
                        "status-pill-success"
                );

            } else {
                applyBadgeStyle(
                        visitedTodayIndicatorLabel,
                        "✖ Сьогодні не був",
                        "status-pill-danger"
                );
            }

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENT_DETAILS_LOAD_VISIT_STATE,
                    UserErrorMessages.VISIT_STATE_LOAD_FAILED,
                    "clientId=" + clientId,
                    e
            );
        }
    }

    @FXML
    private void onViewVisitHistory() {
        if (client == null) {
            return;
        }

        try {
            ViewLoader.openWindow(
                    "/fxml/client/ClientVisitHistoryView.fxml",
                    "Історія відвідувань",
                    0.55,
                    0.7,
                    (ClientVisitHistoryController controller) ->
                            controller.setClient(client)
            );

        } catch (Exception e) {
            ErrorHandler.handle(
                    ErrorLogMessages.CLIENT_VISIT_HISTORY_OPEN,
                    UserErrorMessages.VISIT_HISTORY_OPEN_FAILED,
                    buildClientErrorDetails(),
                    e
            );
        }
    }

    @FXML
    private void onClose() {
        Stage stage =
                (Stage) idValueLabel
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private String buildClientErrorDetails() {
        if (client == null) {
            return "client=null";
        }

        return "clientId=" + client.getId()
                + ", clientNumber=" + client.getClientNumber()
                + ", firstName=" + client.getFirstName()
                + ", lastName=" + client.getLastName();
    }

    private void applyBadgeStyle(
            Label label,
            String text,
            String pillType
    ) {
        label.setText(text);

        label.getStyleClass().setAll(
                "status-pill",
                pillType
        );
    }
}