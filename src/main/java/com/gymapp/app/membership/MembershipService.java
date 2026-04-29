package com.gymapp.app.membership;

import com.gymapp.domain.membership.Membership;
import com.gymapp.domain.membership.MembershipStatus;
import com.gymapp.domain.membership.MembershipType;
import com.gymapp.domain.membership.VisitPolicy;
import com.gymapp.domain.repository.MembershipRepository;

import java.time.LocalDate;
import java.util.Optional;

public class MembershipService {

    private final MembershipRepository membershipRepository;

    public MembershipService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public Optional<Membership> findActiveByClientId(Long clientId) {
        return membershipRepository.findActiveByClientId(clientId);
    }

    public Membership createMembership(Long clientId, MembershipType membershipType, LocalDate startDate) {
        Membership membership = buildMembership(clientId, membershipType, startDate);
        return membershipRepository.save(membership);
    }

    public Membership replaceMembership(Long clientId, MembershipType membershipType, LocalDate startDate) {
        membershipRepository.deactivateActiveByClientId(clientId);

        Membership membership = buildMembership(clientId, membershipType, startDate);
        return membershipRepository.save(membership);
    }

    private Membership buildMembership(Long clientId, MembershipType membershipType, LocalDate startDate) {
        Membership membership = new Membership();
        membership.setClientId(clientId);
        membership.setMembershipTypeId(membershipType.getId());
        membership.setStartDate(startDate);
        membership.setStatus(MembershipStatus.ACTIVE);

        VisitPolicy visitPolicy = membershipType.getVisitPolicy();

        switch (visitPolicy) {
            case LIMITED_BY_VISITS -> {
                membership.setEndDate(
                        membershipType.getDurationDays() != null
                                ? startDate.plusDays(membershipType.getDurationDays())
                                : null
                );
                membership.setRemainingVisits(membershipType.getVisitLimit());
            }
            case LIMITED_BY_TIME, UNLIMITED -> {
                membership.setEndDate(
                        membershipType.getDurationDays() != null
                                ? startDate.plusDays(membershipType.getDurationDays())
                                : null
                );
                membership.setRemainingVisits(null);
            }
        }

        return membership;
    }

    public void expireOutdatedMemberships() {
        membershipRepository.expireOutdatedMemberships(LocalDate.now());
    }

    public Membership createManualMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        Membership membership = buildManualMembership(
                clientId,
                membershipType,
                startDate,
                endDate,
                remainingVisits
        );

        return membershipRepository.save(membership);
    }

    public Membership replaceWithManualMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        membershipRepository.deactivateActiveByClientId(clientId);

        Membership membership = buildManualMembership(
                clientId,
                membershipType,
                startDate,
                endDate,
                remainingVisits
        );

        return membershipRepository.save(membership);
    }

    private Membership buildManualMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        Membership membership = new Membership();
        membership.setClientId(clientId);
        membership.setMembershipTypeId(membershipType.getId());
        membership.setStartDate(startDate);
        membership.setEndDate(endDate);

        if (membershipType.getVisitPolicy() == VisitPolicy.LIMITED_BY_VISITS) {
            membership.setRemainingVisits(remainingVisits);
        } else {
            membership.setRemainingVisits(null);
        }

        if (endDate != null && endDate.isBefore(LocalDate.now())) {
            membership.setStatus(MembershipStatus.EXPIRED);
        } else if (remainingVisits != null && remainingVisits <= 0) {
            membership.setStatus(MembershipStatus.EXPIRED);
        } else {
            membership.setStatus(MembershipStatus.ACTIVE);
        }

        return membership;
    }
}