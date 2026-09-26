package com.gymapp.ui.client.mebmership;

import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.service.MembershipService;

import java.time.LocalDate;

public class ClientMembershipFormSaver {

    private final MembershipService membershipService;

    public ClientMembershipFormSaver(
            MembershipService membershipService
    ) {
        this.membershipService = membershipService;
    }

    public void save(
            Long clientId,
            MembershipType selectedType,
            LocalDate startDate,
            boolean manualMode,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        boolean hasCurrentMembership =
                membershipService
                        .findCurrentByClientId(clientId)
                        .isPresent();

        if (manualMode) {
            saveManual(
                    clientId,
                    selectedType,
                    startDate,
                    endDate,
                    remainingVisits,
                    hasCurrentMembership
            );

            return;
        }

        saveRegular(
                clientId,
                selectedType,
                startDate,
                hasCurrentMembership
        );
    }

    private void saveManual(
            Long clientId,
            MembershipType selectedType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits,
            boolean hasCurrentMembership
    ) {
        if (hasCurrentMembership) {
            membershipService
                    .replaceWithManualMembership(
                            clientId,
                            selectedType,
                            startDate,
                            endDate,
                            remainingVisits
                    );

            return;
        }

        membershipService
                .createManualMembership(
                        clientId,
                        selectedType,
                        startDate,
                        endDate,
                        remainingVisits
                );
    }

    private void saveRegular(
            Long clientId,
            MembershipType selectedType,
            LocalDate startDate,
            boolean hasCurrentMembership
    ) {
        if (hasCurrentMembership) {
            membershipService
                    .replaceMembership(
                            clientId,
                            selectedType,
                            startDate
                    );

            return;
        }

        membershipService
                .createMembership(
                        clientId,
                        selectedType,
                        startDate
                );
    }
}