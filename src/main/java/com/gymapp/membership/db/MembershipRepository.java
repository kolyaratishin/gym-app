package com.gymapp.membership.db;

import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;

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
