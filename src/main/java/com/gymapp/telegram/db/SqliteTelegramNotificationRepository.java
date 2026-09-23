package com.gymapp.telegram.db;

import com.gymapp.db.BaseRepository;
import com.gymapp.db.ConnectionFactory;

public class SqliteTelegramNotificationRepository
        extends BaseRepository
        implements TelegramNotificationRepository {

    public SqliteTelegramNotificationRepository(
            ConnectionFactory connectionFactory
    ) {
        super(connectionFactory);
    }

    @Override
    public TelegramNotification save(
            TelegramNotification notification
    ) {
        String sql = """
                INSERT INTO telegram_notifications (
                    client_id,
                    membership_id,
                    type,
                    reference_value,
                    sent_at
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        long id = insertAndReturnId(
                sql,
                ps -> {
                    ps.setLong(
                            1,
                            notification.getClientId()
                    );

                    ps.setLong(
                            2,
                            notification.getMembershipId()
                    );

                    ps.setString(
                            3,
                            notification.getType().name()
                    );

                    ps.setString(
                            4,
                            notification.getReferenceValue()
                    );

                    ps.setString(
                            5,
                            notification.getSentAt().toString()
                    );
                }
        );

        notification.setId(id);

        return notification;
    }

    @Override
    public boolean exists(
            Long membershipId,
            TelegramNotificationType type,
            String referenceValue
    ) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM telegram_notifications
                    WHERE membership_id = ?
                      AND type = ?
                      AND reference_value = ?
                )
                """;

        return queryForBoolean(
                sql,
                ps -> {
                    ps.setLong(1, membershipId);
                    ps.setString(2, type.name());
                    ps.setString(3, referenceValue);
                }
        );
    }
}