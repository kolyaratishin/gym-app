package com.gymapp.telegram.db;

public interface TelegramNotificationRepository {

    TelegramNotification save(
            TelegramNotification notification
    );

    boolean exists(
            Long membershipId,
            TelegramNotificationType type,
            String referenceValue
    );
}