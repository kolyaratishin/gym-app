package com.gymapp.visit.service;

import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.visit.db.Visit;
import com.gymapp.visit.db.VisitRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class VisitService {

    private final VisitRepository visitRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipTypeService membershipTypeService;

    public VisitService(
            VisitRepository visitRepository,
            MembershipRepository membershipRepository,
            MembershipTypeService membershipTypeService
    ) {
        this.visitRepository = visitRepository;
        this.membershipRepository = membershipRepository;
        this.membershipTypeService = membershipTypeService;
    }

    public String registerVisit(Long clientId) {
        Optional<Membership> membershipOptional = membershipRepository.findActiveByClientId(clientId);

        if (membershipOptional.isEmpty()) {
            return "У клієнта немає активного абонемента";
        }

        Membership membership = membershipOptional.get();

        Optional<MembershipType> membershipTypeOptional = membershipTypeService.findById(membership.getMembershipTypeId());
        if (membershipTypeOptional.isEmpty()) {
            return "Не знайдено тип абонемента";
        }

        MembershipType membershipType = membershipTypeOptional.get();

        if (isExpiredByDate(membership)) {
            membership.setStatus(MembershipStatus.EXPIRED);
            membershipRepository.update(membership);
            return "Абонемент клієнта вже прострочений";
        }

        if (membershipType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            Integer remainingVisits = membership.getRemainingVisits();

            if (remainingVisits == null || remainingVisits <= 0) {
                membership.setStatus(MembershipStatus.EXPIRED);
                membershipRepository.update(membership);
                return "У клієнта закінчилися відвідування";
            }

            membership.setRemainingVisits(remainingVisits - 1);

            if (membership.getRemainingVisits() == 0) {
                membership.setStatus(MembershipStatus.EXPIRED);
            }

            membershipRepository.update(membership);
        }

        Visit visit = new Visit();
        visit.setClientId(clientId);
        visit.setMembershipId(membership.getId());
        visit.setVisitTime(LocalDateTime.now());

        visitRepository.save(visit);

        return "Відвідування успішно зареєстровано";
    }

    private boolean isExpiredByDate(Membership membership) {
        if (membership.getEndDate() == null) {
            return false;
        }

        return membership.getEndDate().isBefore(LocalDate.now());
    }
}