package com.gymapp.ui.client.mebmership;

import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.util.Optional;

public class ClientMembershipCurrentViewBinder {

    private final Label currentMembershipLabel;
    private final Label replacementInfoLabel;

    public ClientMembershipCurrentViewBinder(
            Label currentMembershipLabel,
            Label replacementInfoLabel
    ) {
        this.currentMembershipLabel = currentMembershipLabel;
        this.replacementInfoLabel = replacementInfoLabel;
    }

    public void showCurrentMembership(Optional<Membership> currentMembership) {
        if (currentMembership.isEmpty()) {
            currentMembershipLabel.setText("Немає активного абонемента");
            replacementInfoLabel.setText("Буде створено новий абонемент");
            return;
        }

        currentMembershipLabel.setText(formatCurrentMembership(currentMembership.get()));
        replacementInfoLabel.setText("Поточний активний абонемент буде замінено новим");
    }

    private String formatCurrentMembership(Membership membership) {
        return "Статус: " + formatMembershipStatus(membership.getStatus())
                + ", початок: " + formatDate(membership.getStartDate())
                + ", завершення: " + formatDate(membership.getEndDate())
                + ", залишок: " + formatInteger(membership.getRemainingVisits());
    }

    private String formatMembershipStatus(MembershipStatus status) {
        return switch (status) {
            case ACTIVE -> "Активний";
            case EXPIRED -> "Прострочений";
            case FROZEN -> "Заморожений";
            case CANCELLED -> "Скасований";
        };
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "-";
    }

    private String formatInteger(Integer value) {
        return value != null ? value.toString() : "-";
    }
}