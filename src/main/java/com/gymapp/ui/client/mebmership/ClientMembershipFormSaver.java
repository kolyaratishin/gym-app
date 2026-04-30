package com.gymapp.ui.client.mebmership;

import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.service.MembershipService;

import java.time.LocalDate;

public class ClientMembershipFormSaver {

    private final MembershipService membershipService;

    public ClientMembershipFormSaver(MembershipService membershipService) {
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
        boolean hasActiveMembership = membershipService.findActiveByClientId(clientId).isPresent();

        if (manualMode) {
            saveManual(clientId, selectedType, startDate, endDate, remainingVisits, hasActiveMembership);
            return;
        }

        saveRegular(clientId, selectedType, startDate, hasActiveMembership);
    }

    private void saveManual(
            Long clientId,
            MembershipType selectedType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits,
            boolean hasActiveMembership
    ) {
        if (hasActiveMembership) {
            membershipService.replaceWithManualMembership(
                    clientId,
                    selectedType,
                    startDate,
                    endDate,
                    remainingVisits
            );
        } else {
            membershipService.createManualMembership(
                    clientId,
                    selectedType,
                    startDate,
                    endDate,
                    remainingVisits
            );
        }
    }

    private void saveRegular(
            Long clientId,
            MembershipType selectedType,
            LocalDate startDate,
            boolean hasActiveMembership
    ) {
        if (hasActiveMembership) {
            membershipService.replaceMembership(clientId, selectedType, startDate);
        } else {
            membershipService.createMembership(clientId, selectedType, startDate);
        }
    }
}