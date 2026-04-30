package com.gymapp.membership.service;

import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;

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
        return saveNewMembership(clientId, membershipType, startDate, null, null);
    }

    public Membership replaceMembership(Long clientId, MembershipType membershipType, LocalDate startDate) {
        membershipRepository.deactivateActiveByClientId(clientId);
        return createMembership(clientId, membershipType, startDate);
    }

    public Membership createManualMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        return saveNewMembership(clientId, membershipType, startDate, endDate, remainingVisits);
    }

    public Membership replaceWithManualMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        membershipRepository.deactivateActiveByClientId(clientId);
        return createManualMembership(clientId, membershipType, startDate, endDate, remainingVisits);
    }

    public void expireOutdatedMemberships() {
        membershipRepository.expireOutdatedMemberships(LocalDate.now());
    }

    private Membership saveNewMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate customEndDate,
            Integer customRemainingVisits
    ) {
        Membership membership = buildMembership(
                clientId,
                membershipType,
                startDate,
                customEndDate,
                customRemainingVisits
        );

        return membershipRepository.save(membership);
    }

    private Membership buildMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate customEndDate,
            Integer customRemainingVisits
    ) {
        Membership membership = new Membership();

        membership.setClientId(clientId);
        membership.setMembershipTypeId(membershipType.getId());
        membership.setStartDate(startDate);
        membership.setEndDate(resolveEndDate(membershipType, startDate, customEndDate));
        membership.setRemainingVisits(resolveRemainingVisits(membershipType, customRemainingVisits));
        membership.setStatus(resolveStatus(membership.getEndDate(), membership.getRemainingVisits()));

        return membership;
    }

    private LocalDate resolveEndDate(
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate customEndDate
    ) {
        if (customEndDate != null) {
            return customEndDate;
        }

        Integer durationDays = membershipType.getDurationDays();

        return durationDays != null
                ? startDate.plusDays(durationDays)
                : null;
    }

    private Integer resolveRemainingVisits(
            MembershipType membershipType,
            Integer customRemainingVisits
    ) {
        if (membershipType.getVisitPolicy() != VisitPolicy.LIMITED_BY_VISITS) {
            return null;
        }

        return customRemainingVisits != null
                ? customRemainingVisits
                : membershipType.getVisitLimit();
    }

    private MembershipStatus resolveStatus(LocalDate endDate, Integer remainingVisits) {
        if (endDate != null && endDate.isBefore(LocalDate.now())) {
            return MembershipStatus.EXPIRED;
        }

        if (remainingVisits != null && remainingVisits <= 0) {
            return MembershipStatus.EXPIRED;
        }

        return MembershipStatus.ACTIVE;
    }
}