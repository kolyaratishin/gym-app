package com.gymapp.ui.client.details;

import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;
import com.gymapp.membership.service.MembershipTypeService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class ClientMembershipViewBinder {

    private final MembershipTypeService membershipTypeService;

    private final Label membershipStatusValueLabel;
    private final Label membershipTypeValueLabel;
    private final Label membershipPolicyValueLabel;
    private final Label membershipStartDateValueLabel;
    private final Label membershipEndDateValueLabel;
    private final Label membershipRemainingVisitsValueLabel;
    private final Label membershipPriceValueLabel;
    private final Label membershipDateStateValueLabel;
    private final Label membershipAlertIndicatorLabel;
    private final Button manageMembershipButton;

    public ClientMembershipViewBinder(
            MembershipTypeService membershipTypeService,
            Label membershipStatusValueLabel,
            Label membershipTypeValueLabel,
            Label membershipPolicyValueLabel,
            Label membershipStartDateValueLabel,
            Label membershipEndDateValueLabel,
            Label membershipRemainingVisitsValueLabel,
            Label membershipPriceValueLabel,
            Label membershipDateStateValueLabel,
            Label membershipAlertIndicatorLabel,
            Button manageMembershipButton
    ) {
        this.membershipTypeService = membershipTypeService;
        this.membershipStatusValueLabel = membershipStatusValueLabel;
        this.membershipTypeValueLabel = membershipTypeValueLabel;
        this.membershipPolicyValueLabel = membershipPolicyValueLabel;
        this.membershipStartDateValueLabel = membershipStartDateValueLabel;
        this.membershipEndDateValueLabel = membershipEndDateValueLabel;
        this.membershipRemainingVisitsValueLabel = membershipRemainingVisitsValueLabel;
        this.membershipPriceValueLabel = membershipPriceValueLabel;
        this.membershipDateStateValueLabel = membershipDateStateValueLabel;
        this.membershipAlertIndicatorLabel = membershipAlertIndicatorLabel;
        this.manageMembershipButton = manageMembershipButton;
    }

    public void showMembership(Optional<Membership> membershipOptional) {
        if (membershipOptional.isEmpty()) {
            showNoActiveMembership();
            return;
        }

        showActiveMembership(membershipOptional.get());
    }

    private void showNoActiveMembership() {
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
    }

    private void showActiveMembership(Membership membership) {
        membershipStatusValueLabel.setText(formatMembershipStatus(membership.getStatus()));
        membershipStartDateValueLabel.setText(formatDate(membership.getStartDate()));
        membershipEndDateValueLabel.setText(formatDate(membership.getEndDate()));
        membershipRemainingVisitsValueLabel.setText(formatInteger(membership.getRemainingVisits()));
        membershipDateStateValueLabel.setText(resolveDateState(membership));

        membershipTypeService.findById(membership.getMembershipTypeId())
                .ifPresentOrElse(
                        membershipType -> showMembershipTypeInfo(membership, membershipType),
                        this::showMissingMembershipTypeInfo
                );

        manageMembershipButton.setText("Замінити абонемент");
    }

    private void showMembershipTypeInfo(Membership membership, MembershipType membershipType) {
        membershipTypeValueLabel.setText(nullToDash(membershipType.getName()));
        membershipPolicyValueLabel.setText(formatVisitPolicy(membershipType.getVisitPolicy()));
        membershipPriceValueLabel.setText(formatPrice(membershipType.getPrice()));

        updateMembershipAlertIndicator(membership, membershipType);
    }

    private void showMissingMembershipTypeInfo() {
        membershipTypeValueLabel.setText("-");
        membershipPolicyValueLabel.setText("-");
        membershipPriceValueLabel.setText("-");
        membershipDateStateValueLabel.setText("-");
        applyBadgeStyle(membershipAlertIndicatorLabel, "✖ Немає даних", "status-pill-danger");
    }

    private void updateMembershipAlertIndicator(Membership membership, MembershipType membershipType) {
        if (membership.getStatus() == MembershipStatus.EXPIRED) {
            applyBadgeStyle(membershipAlertIndicatorLabel, "✖ Прострочений", "status-pill-danger");
            return;
        }

        if (membershipType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            updateVisitBasedMembershipAlert(membership);
            return;
        }

        updateDateBasedMembershipAlert(membership);
    }

    private void updateVisitBasedMembershipAlert(Membership membership) {
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
    }

    private void updateDateBasedMembershipAlert(Membership membership) {
        if (membership.getEndDate() == null) {
            applyBadgeStyle(membershipAlertIndicatorLabel, "✔ Абонемент ок", "status-pill-success");
            return;
        }

        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), membership.getEndDate());

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
        if (visitPolicy == null) {
            return "-";
        }

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

        LocalDate today = LocalDate.now();

        if (membership.getEndDate().isBefore(today)) {
            return "Прострочений";
        }

        if (membership.getEndDate().isEqual(today)) {
            return "Останній день";
        }

        return "Активний";
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "-";
    }

    private String formatInteger(Integer value) {
        return value != null ? value.toString() : "-";
    }

    private String formatPrice(BigDecimal price) {
        return price != null ? price.toPlainString() : "-";
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void applyBadgeStyle(Label label, String text, String pillType) {
        label.setText(text);
        label.getStyleClass().setAll("status-pill", pillType);
    }
}