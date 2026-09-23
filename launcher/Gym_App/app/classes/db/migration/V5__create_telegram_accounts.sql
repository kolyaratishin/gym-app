CREATE TABLE telegram_accounts (
                                   client_id INTEGER PRIMARY KEY,
                                   telegram_user_id INTEGER NOT NULL UNIQUE,
                                   telegram_chat_id INTEGER NOT NULL UNIQUE,
                                   username TEXT,
                                   linked_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   FOREIGN KEY (client_id)
                                       REFERENCES clients(id)
                                       ON DELETE CASCADE
);