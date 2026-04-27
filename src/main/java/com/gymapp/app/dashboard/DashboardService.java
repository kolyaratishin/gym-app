package com.gymapp.app.dashboard;

import com.gymapp.domain.repository.ClientRepository;
import com.gymapp.domain.repository.MembershipRepository;
import com.gymapp.domain.repository.VisitRepository;

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