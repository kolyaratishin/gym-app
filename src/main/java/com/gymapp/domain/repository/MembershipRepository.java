package com.gymapp.domain.repository;

import com.gymapp.domain.membership.Membership;
import com.gymapp.domain.membership.MembershipStatus;

import com.gymapp.domain.membership.MembershipType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository {

    Membership save(Membership membership);

    Optional<Membership> findById(Long id);

    Optional<Membership> findActiveByClientId(Long clientId);

    List<Membership> findAll();

    List<Membership> findByClientId(Long clientId);

    List<Membership> findByStatus(MembershipStatus status);

    List<Membership> findExpiringUntil(LocalDate date);

    void update(Membership membership);

    void expireById(Long membershipId);

    void deactivateActiveByClientId(Long clientId);

    void expireOutdatedMemberships(LocalDate today);

    long countClientsWithActiveMembership();
}
