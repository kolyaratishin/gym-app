package com.gymapp.membership.db;

import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository {

    Membership save(Membership membership);

    Membership save(
            Connection connection,
            Membership membership
    );

    Optional<Membership> findById(Long id);

    Optional<Membership> findActiveByClientId(Long clientId);

    Optional<Membership> findCurrentByClientId(Long clientId);

    List<Membership> findAll();

    List<Membership> findByClientId(Long clientId);

    List<Membership> findByStatus(MembershipStatus status);

    List<Membership> findExpiringUntil(LocalDate date);

    void update(Membership membership);

    void update(
            Connection connection,
            Membership membership
    );

    void expireById(Long membershipId);

    void deactivateActiveByClientId(Long clientId);

    void deactivateActiveByClientId(
            Connection connection,
            Long clientId
    );

    void expireOutdatedMemberships(LocalDate today);

    void activateScheduledMemberships(LocalDate today);

    long countClientsWithActiveMembership();

    List<Membership> findExpiringBetween(
            LocalDate from,
            LocalDate to
    );

    List<Membership> findExpired();
}