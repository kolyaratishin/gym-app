package com.gymapp.visit.service;

import com.gymapp.db.ConnectionFactory;
import com.gymapp.membership.db.MembershipRepository;
import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.db.domain.MembershipStatus;
import com.gymapp.membership.db.domain.MembershipType;
import com.gymapp.membership.db.domain.VisitPolicy;
import com.gymapp.membership.service.MembershipTypeService;
import com.gymapp.telegram.service.TelegramNotificationService;
import com.gymapp.visit.db.Visit;
import com.gymapp.visit.db.VisitRepository;
import com.gymapp.visit.dto.ClientVisitHistoryRow;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class VisitService {

    private final VisitRepository visitRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipTypeService membershipTypeService;
    private final TelegramNotificationService telegramNotificationService;
    private final ConnectionFactory connectionFactory;

    public VisitService(
            VisitRepository visitRepository,
            MembershipRepository membershipRepository,
            MembershipTypeService membershipTypeService,
            TelegramNotificationService telegramNotificationService,
            ConnectionFactory connectionFactory
    ) {
        this.visitRepository = visitRepository;
        this.membershipRepository = membershipRepository;
        this.membershipTypeService = membershipTypeService;
        this.telegramNotificationService = telegramNotificationService;
        this.connectionFactory = connectionFactory;
    }

    public String registerVisit(Long clientId) {
        Optional<Membership> membershipOptional =
                membershipRepository.findActiveByClientId(
                        clientId
                );

        if (membershipOptional.isEmpty()) {
            Optional<Membership> currentMembership =
                    membershipRepository.findCurrentByClientId(
                            clientId
                    );

            if (currentMembership.isPresent()) {
                Membership membership =
                        currentMembership.get();

                if (membership.getStatus()
                        == MembershipStatus.SCHEDULED) {

                    return "Абонемент клієнта ще не почав діяти. "
                            + "Дата початку: "
                            + membership.getStartDate();
                }

                if (membership.getStatus()
                        == MembershipStatus.FROZEN) {

                    return "Абонемент клієнта заморожений";
                }
            }

            return "У клієнта немає активного абонемента";
        }

        Membership membership =
                membershipOptional.get();

        if (isNotStartedYet(membership)) {
            return "Абонемент клієнта ще не почав діяти. "
                    + "Дата початку: "
                    + membership.getStartDate();
        }

        Optional<MembershipType> membershipTypeOptional =
                membershipTypeService.findById(
                        membership.getMembershipTypeId()
                );

        if (membershipTypeOptional.isEmpty()) {
            return "Не знайдено тип абонемента";
        }

        MembershipType membershipType =
                membershipTypeOptional.get();

        if (isExpiredByDate(membership)) {
            membership.setStatus(
                    MembershipStatus.EXPIRED
            );

            membershipRepository.update(
                    membership
            );

            return "Абонемент клієнта вже прострочений";
        }

        /*
         * До цього моменту ми тільки перевіряли дані.
         *
         * Зміни membership + створення visit
         * виконуються нижче в одній транзакції.
         */

        if (membershipType.getVisitPolicy()
                == VisitPolicy.LIMITED_BY_VISITS) {

            Integer remainingVisits =
                    membership.getRemainingVisits();

            if (remainingVisits == null
                    || remainingVisits <= 0) {

                membership.setStatus(
                        MembershipStatus.EXPIRED
                );

                membershipRepository.update(
                        membership
                );

                return "У клієнта закінчилися відвідування";
            }

            membership.setRemainingVisits(
                    remainingVisits - 1
            );

            if (membership.getRemainingVisits() == 0) {
                membership.setStatus(
                        MembershipStatus.EXPIRED
                );
            }
        }

        Visit visit =
                new Visit();

        visit.setClientId(
                clientId
        );

        visit.setMembershipId(
                membership.getId()
        );

        visit.setVisitTime(
                LocalDateTime.now()
        );

        /*
         * Критична частина операції.
         *
         * Membership update та Visit insert
         * використовують один Connection.
         */
        try (Connection connection =
                     connectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try {
                if (membershipType.getVisitPolicy()
                        == VisitPolicy.LIMITED_BY_VISITS) {

                    membershipRepository.update(
                            connection,
                            membership
                    );
                }

                visitRepository.save(
                        connection,
                        visit
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
                        "Не вдалося зареєструвати відвідування",
                        e
                );
            }

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Не вдалося виконати транзакцію відвідування",
                    e
            );
        }

        /*
         * Telegram навмисно ПІСЛЯ commit.
         *
         * Помилка Telegram не повинна
         * rollback-нути успішне відвідування.
         */
        sendVisitNotificationSilently(
                membership,
                membershipType
        );

        return "Відвідування успішно зареєстровано";
    }

    private boolean isNotStartedYet(
            Membership membership
    ) {
        LocalDate startDate =
                membership.getStartDate();

        return startDate != null
                && startDate.isAfter(
                LocalDate.now()
        );
    }

    private boolean isExpiredByDate(
            Membership membership
    ) {
        if (membership.getEndDate() == null) {
            return false;
        }

        return membership.getEndDate()
                .isBefore(LocalDate.now());
    }

    private void sendVisitNotificationSilently(
            Membership membership,
            MembershipType membershipType
    ) {
        if (telegramNotificationService == null) {
            return;
        }

        if (membershipType.getVisitPolicy()
                != VisitPolicy.LIMITED_BY_VISITS) {

            return;
        }

        try {
            Integer remainingVisits =
                    membership.getRemainingVisits();

            if (remainingVisits != null
                    && remainingVisits == 0) {

                telegramNotificationService
                        .sendVisitsExhaustedNotification(
                                membership
                        );

                return;
            }

            telegramNotificationService
                    .sendLowVisitsNotification(
                            membership
                    );

        } catch (Exception e) {
            System.err.println(
                    "Failed to send visit Telegram notification "
                            + "for membershipId="
                            + membership.getId()
            );

            e.printStackTrace();
        }
    }

    public Boolean hasVisitToday(Long clientId) {
        return visitRepository.hasVisitToday(
                clientId
        );
    }

    public List<Visit> findByClientId(
            Long clientId
    ) {
        return visitRepository.findByClientId(
                clientId
        );
    }

    public List<ClientVisitHistoryRow> findHistoryByClientId(
            Long clientId
    ) {
        return visitRepository.findHistoryByClientId(
                clientId
        );
    }
}