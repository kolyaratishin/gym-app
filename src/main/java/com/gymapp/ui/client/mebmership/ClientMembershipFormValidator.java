package com.gymapp.ui.client.mebmership;

import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;

import java.time.LocalDate;

public class ClientMembershipFormValidator {

    public String validate(
            Long clientId,
            MembershipType selectedType,
            LocalDate startDate,
            boolean manualMode,
            LocalDate endDate,
            String remainingVisitsText
    ) {
        if (clientId == null) {
            return "Клієнт не вибраний";
        }

        if (selectedType == null) {
            return "Потрібно вибрати тип абонемента";
        }

        if (startDate == null) {
            return "Потрібно вибрати дату початку";
        }

        if (!manualMode) {
            return null;
        }

        return validateManual(selectedType, startDate, endDate, remainingVisitsText);
    }

    private String validateManual(
            MembershipType selectedType,
            LocalDate startDate,
            LocalDate endDate,
            String remainingVisitsText
    ) {
        if (endDate == null) {
            return "Потрібно вказати дату завершення";
        }

        if (endDate.isBefore(startDate)) {
            return "Дата завершення не може бути раніше дати початку";
        }

        if (selectedType.getVisitPolicy() != VisitPolicy.LIMITED_BY_VISITS) {
            return null;
        }

        return validateRemainingVisits(remainingVisitsText);
    }

    private String validateRemainingVisits(String value) {
        String trimmed = value == null ? "" : value.trim();

        if (trimmed.isEmpty()) {
            return "Потрібно вказати залишок тренувань";
        }

        try {
            int parsed = Integer.parseInt(trimmed);

            if (parsed < 0) {
                return "Залишок тренувань не може бути від'ємним";
            }

            return null;
        } catch (NumberFormatException e) {
            return "Залишок тренувань має бути числом";
        }
    }
}