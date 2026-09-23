package com.gymapp.telegram.db;

import com.gymapp.db.BaseRepository;
import com.gymapp.db.ConnectionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SqliteTelegramAccountRepository
        extends BaseRepository
        implements TelegramAccountRepository {

    public SqliteTelegramAccountRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void save(TelegramAccount account) {
        String sql = """
                INSERT INTO telegram_accounts (
                    client_id,
                    telegram_user_id,
                    telegram_chat_id,
                    username
                )
                VALUES (?, ?, ?, ?)
                """;

        update(sql, ps -> {
            ps.setLong(1, account.getClientId());
            ps.setLong(2, account.getTelegramUserId());
            ps.setLong(3, account.getTelegramChatId());
            ps.setString(4, account.getUsername());
        });
    }

    @Override
    public Optional<TelegramAccount> findByClientId(Long clientId) {
        String sql = """
                SELECT *
                FROM telegram_accounts
                WHERE client_id = ?
                """;

        List<TelegramAccount> result = query(
                sql,
                ps -> ps.setLong(1, clientId),
                this::map
        );

        return result.stream().findFirst();
    }

    @Override
    public Optional<TelegramAccount> findByTelegramUserId(Long telegramUserId) {
        String sql = """
                SELECT *
                FROM telegram_accounts
                WHERE telegram_user_id = ?
                """;

        List<TelegramAccount> result = query(
                sql,
                ps -> ps.setLong(1, telegramUserId),
                this::map
        );

        return result.stream().findFirst();
    }

    @Override
    public List<TelegramAccount> findAll() {
        return query(
                "SELECT * FROM telegram_accounts ORDER BY linked_at",
                null,
                this::map
        );
    }

    private TelegramAccount map(ResultSet rs) throws SQLException {
        return new TelegramAccount(
                rs.getLong("client_id"),
                rs.getLong("telegram_user_id"),
                rs.getLong("telegram_chat_id"),
                rs.getString("username"),
                LocalDateTime.parse(
                        rs.getString("linked_at").replace(" ", "T")
                )
        );
    }
}