package com.gymapp.membership.service;

import com.gymapp.audit.ActivityLogger;
import com.gymapp.audit.AuditEventType;
import com.gymapp.db.ConnectionFactory;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final ConnectionFactory connectionFactory;

    public MembershipService(
            MembershipRepository membershipRepository,
            ConnectionFactory connectionFactory
    ) {
        this.membershipRepository =
                membershipRepository;

        this.connectionFactory =
                connectionFactory;
    }

    public Optional<Membership> findActiveByClientId(
            Long clientId
    ) {
        return membershipRepository
                .findActiveByClientId(clientId);
    }

    public Optional<Membership> findCurrentByClientId(
            Long clientId
    ) {
        return membershipRepository
                .findCurrentByClientId(clientId);
    }

    public Membership createMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate
    ) {
        Membership membership =
                saveNewMembership(
                        clientId,
                        membershipType,
                        startDate,
                        null,
                        null
                );

        logMembershipCreated(
                clientId
        );

        return membership;
    }

    public Membership replaceMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate
    ) {
        Membership membership =
                buildMembership(
                        clientId,
                        membershipType,
                        startDate,
                        null,
                        null
                );

        replaceMembershipInTransaction(
                clientId,
                membership
        );

        logMembershipCreated(
                clientId
        );

        return membership;
    }

    public Membership createManualMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        return saveNewMembership(
                clientId,
                membershipType,
                startDate,
                endDate,
                remainingVisits
        );
    }

    public Membership replaceWithManualMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        Membership membership =
                buildMembership(
                        clientId,
                        membershipType,
                        startDate,
                        endDate,
                        remainingVisits
                );

        replaceMembershipInTransaction(
                clientId,
                membership
        );

        return membership;
    }

    private void replaceMembershipInTransaction(
            Long clientId,
            Membership newMembership
    ) {
        try (Connection connection =
                     connectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                membershipRepository
                        .deactivateActiveByClientId(
                                connection,
                                clientId
                        );

                membershipRepository.save(
                        connection,
                        newMembership
                );

                connection.commit();

            } catch (Exception e) {
                try {
                    connection.rollback();

                } catch (Exception rollbackException) {
                    e.addSuppressed(
                            rollbackException
                    );
                }

                throw new RuntimeException(
                        "Не вдалося замінити абонемент",
                        e
                );
            }

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Не вдалося виконати транзакцію заміни абонемента",
                    e
            );
        }
    }

    public Membership freezeMembership(
            Long membershipId
    ) {
        Membership membership =
                membershipRepository
                        .findById(membershipId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Membership not found: "
                                                        + membershipId
                                        )
                        );

        if (membership.getStatus()
                != MembershipStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Заморозити можна тільки активний абонемент"
            );
        }

        if (membership.getEndDate() == null) {
            throw new IllegalStateException(
                    "Абонемент без дати завершення не можна заморозити"
            );
        }

        membership.setStatus(
                MembershipStatus.FROZEN
        );

        membership.setPausedAt(
                LocalDateTime.now()
        );

        membershipRepository.update(
                membership
        );

        ActivityLogger.log(
                AuditEventType.MEMBERSHIP_FROZEN,
                "Заморожено абонемент: membershipId="
                        + membership.getId()
                        + ", clientId="
                        + membership.getClientId()
                        + ", pausedAt="
                        + membership.getPausedAt()
                        + ", endDate="
                        + membership.getEndDate()
        );

        return membership;
    }

    public Membership resumeMembership(
            Long membershipId
    ) {
        Membership membership =
                membershipRepository
                        .findById(membershipId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Membership not found: "
                                                        + membershipId
                                        )
                        );

        if (membership.getStatus()
                != MembershipStatus.FROZEN) {

            throw new IllegalStateException(
                    "Відновити можна тільки заморожений абонемент"
            );
        }

        if (membership.getPausedAt() == null) {
            throw new IllegalStateException(
                    "Для абонемента відсутня дата заморозки"
            );
        }

        if (membership.getEndDate() == null) {
            throw new IllegalStateException(
                    "Для абонемента відсутня дата завершення"
            );
        }

        LocalDate pausedDate =
                membership.getPausedAt()
                        .toLocalDate();

        LocalDate resumedDate =
                LocalDate.now();

        long frozenDays =
                ChronoUnit.DAYS.between(
                        pausedDate,
                        resumedDate
                );

        if (frozenDays > 0) {
            membership.setEndDate(
                    membership.getEndDate()
                            .plusDays(frozenDays)
            );
        }

        membership.setStatus(
                MembershipStatus.ACTIVE
        );

        membership.setPausedAt(null);

        membershipRepository.update(
                membership
        );

        ActivityLogger.log(
                AuditEventType.MEMBERSHIP_RESUMED,
                "Відновлено абонемент: membershipId="
                        + membership.getId()
                        + ", clientId="
                        + membership.getClientId()
                        + ", frozenDays="
                        + frozenDays
                        + ", newEndDate="
                        + membership.getEndDate()
        );

        return membership;
    }

    public void synchronizeMembershipStatuses() {
        LocalDate today =
                LocalDate.now();

        membershipRepository
                .activateScheduledMemberships(
                        today
                );

        membershipRepository
                .expireOutdatedMemberships(
                        today
                );
    }

    public void expireOutdatedMemberships() {
        membershipRepository
                .expireOutdatedMemberships(
                        LocalDate.now()
                );
    }

    public List<Membership> findExpiringBetween(
            LocalDate from,
            LocalDate to
    ) {
        return membershipRepository
                .findExpiringBetween(
                        from,
                        to
                );
    }

    public List<Membership> findExpired() {
        return membershipRepository
                .findExpired();
    }

    private Membership saveNewMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate customEndDate,
            Integer customRemainingVisits
    ) {
        Membership membership =
                buildMembership(
                        clientId,
                        membershipType,
                        startDate,
                        customEndDate,
                        customRemainingVisits
                );

        return membershipRepository.save(
                membership
        );
    }

    private Membership buildMembership(
            Long clientId,
            MembershipType membershipType,
            LocalDate startDate,
            LocalDate customEndDate,
            Integer customRemainingVisits
    ) {
        Membership membership =
                new Membership();

        membership.setClientId(
                clientId
        );

        membership.setMembershipTypeId(
                membershipType.getId()
        );

        membership.setStartDate(
                startDate
        );

        membership.setEndDate(
                resolveEndDate(
                        membershipType,
                        startDate,
                        customEndDate
                )
        );

        membership.setRemainingVisits(
                resolveRemainingVisits(
                        membershipType,
                        customRemainingVisits
                )
        );

        membership.setStatus(
                resolveStatus(
                        membership.getStartDate(),
                        membership.getEndDate(),
                        membership.getRemainingVisits()
                )
        );

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

        Integer durationDays =
                membershipType.getDurationDays();

        return durationDays != null
                ? startDate.plusDays(durationDays)
                : null;
    }

    private Integer resolveRemainingVisits(
            MembershipType membershipType,
            Integer customRemainingVisits
    ) {
        if (membershipType.getVisitPolicy()
                != VisitPolicy.LIMITED_BY_VISITS) {

            return null;
        }

        return customRemainingVisits != null
                ? customRemainingVisits
                : membershipType.getVisitLimit();
    }

    private MembershipStatus resolveStatus(
            LocalDate startDate,
            LocalDate endDate,
            Integer remainingVisits
    ) {
        LocalDate today =
                LocalDate.now();

        if (endDate != null
                && endDate.isBefore(today)) {

            return MembershipStatus.EXPIRED;
        }

        if (remainingVisits != null
                && remainingVisits <= 0) {

            return MembershipStatus.EXPIRED;
        }

        if (startDate.isAfter(today)) {
            return MembershipStatus.SCHEDULED;
        }

        return MembershipStatus.ACTIVE;
    }

    private void logMembershipCreated(
            Long clientId
    ) {
        ActivityLogger.log(
                AuditEventType.MEMBERSHIP_CREATED,
                "Створено абонемент для clientId="
                        + clientId
        );
    }
}