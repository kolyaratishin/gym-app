package com.gymapp.telegram.service;

import com.gymapp.membership.db.domain.Membership;
import com.gymapp.membership.service.MembershipService;
import com.gymapp.telegram.bot.GymTelegramBot;
import com.gymapp.telegram.db.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class TelegramNotificationService {

    private static final int EXPIRING_DAYS_BEFORE = 3;
    private static final int LOW_VISITS_THRESHOLD = 2;
    private static final String LOW_VISITS_REFERENCE = "LOW";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final MembershipService membershipService;
    private final TelegramAccountRepository telegramAccountRepository;
    private final TelegramNotificationRepository notificationRepository;
    private final GymTelegramBot telegramBot;

    public TelegramNotificationService(
            MembershipService membershipService,
            TelegramAccountRepository telegramAccountRepository,
            TelegramNotificationRepository notificationRepository,
            GymTelegramBot telegramBot
    ) {
        this.membershipService = membershipService;
        this.telegramAccountRepository = telegramAccountRepository;
        this.notificationRepository = notificationRepository;
        this.telegramBot = telegramBot;
    }

    public NotificationResult sendExpiringMembershipNotifications() {
        LocalDate today = LocalDate.now();
        LocalDate maxEndDate =
                today.plusDays(EXPIRING_DAYS_BEFORE);

        List<Membership> memberships =
                membershipService.findExpiringBetween(
                        today,
                        maxEndDate
                );

        int sent = 0;
        int skipped = 0;
        int failed = 0;

        for (Membership membership : memberships) {
            LocalDate endDate = membership.getEndDate();
            String referenceValue = endDate.toString();

            if (alreadySent(
                    membership,
                    TelegramNotificationType.MEMBERSHIP_EXPIRING,
                    referenceValue
            )) {
                skipped++;
                continue;
            }

            Optional<TelegramAccount> telegramAccount =
                    telegramAccountRepository.findByClientId(
                            membership.getClientId()
                    );

            if (telegramAccount.isEmpty()) {
                skipped++;
                continue;
            }

            boolean success = telegramBot.sendMessage(
                    telegramAccount.get().getTelegramChatId(),
                    buildExpiringMessage(today, endDate)
            );

            if (!success) {
                failed++;
                continue;
            }

            saveNotification(
                    membership,
                    TelegramNotificationType.MEMBERSHIP_EXPIRING,
                    referenceValue
            );

            sent++;
        }

        return new NotificationResult(
                memberships.size(),
                sent,
                skipped,
                failed
        );
    }

    public NotificationResult sendExpiredMembershipNotifications() {
        List<Membership> memberships = membershipService.findExpired();

        int sent = 0;
        int skipped = 0;
        int failed = 0;

        for (Membership membership : memberships) {
            LocalDate endDate = membership.getEndDate();
            String referenceValue = endDate.toString();

            if (alreadySent(
                    membership,
                    TelegramNotificationType.MEMBERSHIP_EXPIRED,
                    referenceValue
            )) {
                skipped++;
                continue;
            }

            boolean hasActiveMembership =
                    membershipService
                            .findActiveByClientId(
                                    membership.getClientId()
                            )
                            .isPresent();

            if (hasActiveMembership) {
                skipped++;
                continue;
            }

            Optional<TelegramAccount> telegramAccount =
                    telegramAccountRepository.findByClientId(
                            membership.getClientId()
                    );

            if (telegramAccount.isEmpty()) {
                skipped++;
                continue;
            }

            boolean success = telegramBot.sendMessage(
                    telegramAccount.get().getTelegramChatId(),
                    buildExpiredMessage()
            );

            if (!success) {
                failed++;
                continue;
            }

            saveNotification(
                    membership,
                    TelegramNotificationType.MEMBERSHIP_EXPIRED,
                    referenceValue
            );

            sent++;
        }

        return new NotificationResult(
                memberships.size(),
                sent,
                skipped,
                failed
        );
    }

    public boolean sendLowVisitsNotification(
            Membership membership
    ) {
        Integer remainingVisits =
                membership.getRemainingVisits();

        if (remainingVisits == null
                || remainingVisits <= 0
                || remainingVisits > LOW_VISITS_THRESHOLD) {
            return false;
        }

        if (alreadySent(
                membership,
                TelegramNotificationType.LOW_VISITS,
                LOW_VISITS_REFERENCE
        )) {
            return false;
        }

        Optional<TelegramAccount> telegramAccount =
                telegramAccountRepository.findByClientId(
                        membership.getClientId()
                );

        if (telegramAccount.isEmpty()) {
            return false;
        }

        boolean success = telegramBot.sendMessage(
                telegramAccount.get().getTelegramChatId(),
                buildLowVisitsMessage(remainingVisits)
        );

        if (!success) {
            return false;
        }

        saveNotification(
                membership,
                TelegramNotificationType.LOW_VISITS,
                LOW_VISITS_REFERENCE
        );

        return true;
    }

    private boolean alreadySent(
            Membership membership,
            TelegramNotificationType type,
            String referenceValue
    ) {
        return notificationRepository.exists(
                membership.getId(),
                type,
                referenceValue
        );
    }

    private void saveNotification(
            Membership membership,
            TelegramNotificationType type,
            String referenceValue
    ) {
        TelegramNotification notification =
                new TelegramNotification();

        notification.setClientId(
                membership.getClientId()
        );

        notification.setMembershipId(
                membership.getId()
        );

        notification.setType(type);
        notification.setReferenceValue(referenceValue);
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    private String buildExpiringMessage(
            LocalDate today,
            LocalDate endDate
    ) {
        long daysLeft =
                ChronoUnit.DAYS.between(today, endDate);

        String expirationText = switch ((int) daysLeft) {
            case 0 -> "сьогодні";
            case 1 -> "завтра";
            case 2 -> "через 2 дні";
            case 3 -> "через 3 дні";
            default -> endDate.format(DATE_FORMATTER);
        };

        return """
                Нагадування 🏋️

                Ваш абонемент закінчується %s — %s.

                Будемо раді бачити вас у залі!
                """.formatted(
                expirationText,
                endDate.format(DATE_FORMATTER)
        );
    }

    private String buildExpiredMessage() {
        return """
                Ваш абонемент закінчився 🏋️

                Будемо раді бачити вас знову!

                Продовжіть абонемент, щоб повернутися до тренувань 💪
                """;
    }

    private String buildLowVisitsMessage(
            int remainingVisits
    ) {
        return """
                Нагадування 🏋️

                У вашому абонементі залишилося %d відвідування.

                Не забудьте завчасно продовжити абонемент 💪
                """.formatted(remainingVisits);
    }

    public record NotificationResult(
            int found,
            int sent,
            int skipped,
            int failed
    ) {}
}