package com.gymapp.dashboard.service;

import com.gymapp.client.db.ClientRepository;
import com.gymapp.dashboard.dto.DashboardStats;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.visit.db.VisitRepository;

import java.time.LocalDate;

public class DashboardService {

    private final ClientRepository clientRepository;
    private final VisitRepository visitRepository;
    private final MembershipRepository membershipRepository;

    public DashboardService(
            ClientRepository clientRepository,
            VisitRepository visitRepository,
            MembershipRepository membershipRepository
    ) {
        this.clientRepository = clientRepository;
        this.visitRepository = visitRepository;
        this.membershipRepository = membershipRepository;
    }

    public DashboardStats getStats() {
        long totalClients = clientRepository.countAll();
        long activeClients = membershipRepository.countClientsWithActiveMembership();
        long visitsToday = visitRepository.countByDate(LocalDate.now());
        long expiringMemberships = membershipRepository.findExpiringUntil(LocalDate.now().plusDays(7)).size();

        return new DashboardStats(
                totalClients,
                activeClients,
                visitsToday,
                expiringMemberships
        );
    }
}