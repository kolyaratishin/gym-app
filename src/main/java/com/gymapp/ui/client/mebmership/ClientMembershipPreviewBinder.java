package com.gymapp.ui.client.mebmership;

import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;
import javafx.scene.control.Label;

import java.time.LocalDate;

public class ClientMembershipPreviewBinder {

    private final Label previewPolicyLabel;
    private final Label previewEndDateLabel;
    private final Label previewRemainingVisitsLabel;
    private final Label previewStatusLabel;

    public ClientMembershipPreviewBinder(
            Label previewPolicyLabel,
            Label previewEndDateLabel,
            Label previewRemainingVisitsLabel,
            Label previewStatusLabel
    ) {
        this.previewPolicyLabel = previewPolicyLabel;
        this.previewEndDateLabel = previewEndDateLabel;
        this.previewRemainingVisitsLabel = previewRemainingVisitsLabel;
        this.previewStatusLabel = previewStatusLabel;
    }

    public void update(
            MembershipType selectedType,
            LocalDate startDate,
            boolean manualMode,
            LocalDate manualEndDate,
            String manualRemainingVisits
    ) {
        if (selectedType == null || startDate == null) {
            clear();
            return;
        }

        previewPolicyLabel.setText(formatVisitPolicy(selectedType.getVisitPolicy()));
        previewStatusLabel.setText("Активний");

        if (manualMode) {
            showManualPreview(selectedType, manualEndDate, manualRemainingVisits);
        } else {
            showRegularPreview(selectedType, startDate);
        }
    }

    public void clear() {
        previewPolicyLabel.setText("-");
        previewEndDateLabel.setText("-");
        previewRemainingVisitsLabel.setText("-");
        previewStatusLabel.setText("-");
    }

    private void showManualPreview(
            MembershipType selectedType,
            LocalDate manualEndDate,
            String manualRemainingVisits
    ) {
        previewEndDateLabel.setText(formatDate(manualEndDate));

        if (selectedType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            previewRemainingVisitsLabel.setText(formatTextOrDash(manualRemainingVisits));
        } else {
            previewRemainingVisitsLabel.setText("-");
        }
    }

    private void showRegularPreview(MembershipType selectedType, LocalDate startDate) {
        previewEndDateLabel.setText(resolveEndDateText(selectedType, startDate));

        if (selectedType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            previewRemainingVisitsLabel.setText(formatInteger(selectedType.getVisitLimit()));
        } else {
            previewRemainingVisitsLabel.setText("-");
        }
    }

    private String resolveEndDateText(MembershipType selectedType, LocalDate startDate) {
        if (selectedType.getDurationDays() == null) {
            return "-";
        }

        return startDate.plusDays(selectedType.getDurationDays()).toString();
    }

    private String formatVisitPolicy(VisitPolicy visitPolicy) {
        return switch (visitPolicy) {
            case UNLIMITED -> "Безлімітний";
            case LIMITED_BY_VISITS -> "За кількістю відвідувань";
            case LIMITED_BY_TIME -> "За часом";
        };
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "-";
    }

    private String formatInteger(Integer value) {
        return value != null ? value.toString() : "-";
    }

    private String formatTextOrDash(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? "-" : trimmed;
    }
}