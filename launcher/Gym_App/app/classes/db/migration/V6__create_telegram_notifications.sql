CREATE TABLE telegram_notifications (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        client_id INTEGER NOT NULL,
                                        membership_id INTEGER NOT NULL,
                                        type TEXT NOT NULL,
                                        target_date TEXT NOT NULL,
                                        sent_at TEXT NOT NULL,

                                        FOREIGN KEY (client_id)
                                            REFERENCES clients(id)
                                            ON DELETE CASCADE,

                                        FOREIGN KEY (membership_id)
                                            REFERENCES memberships(id)
                                            ON DELETE CASCADE,

                                        UNIQUE (membership_id, type, target_date)
);

CREATE INDEX idx_telegram_notifications_membership
    ON telegram_notifications(membership_id);