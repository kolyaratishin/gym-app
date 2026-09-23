package com.gymapp.telegram.db;

import java.util.List;
import java.util.Optional;

public interface TelegramAccountRepository {

    void save(TelegramAccount account);

    Optional<TelegramAccount> findByClientId(Long clientId);

    Optional<TelegramAccount> findByTelegramUserId(Long telegramUserId);

    List<TelegramAccount> findAll();
}