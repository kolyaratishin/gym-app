package com.gymapp.telegram.service;

import com.gymapp.client.db.Client;
import com.gymapp.client.service.ClientService;
import com.gymapp.telegram.db.TelegramAccount;
import com.gymapp.telegram.db.TelegramAccountRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public class TelegramLinkService {

    private final ClientService clientService;
    private final TelegramAccountRepository telegramAccountRepository;

    public TelegramLinkService(
            ClientService clientService,
            TelegramAccountRepository telegramAccountRepository
    ) {
        this.clientService = clientService;
        this.telegramAccountRepository = telegramAccountRepository;
    }

    public LinkResult link(
            String phone,
            Long telegramUserId,
            Long telegramChatId,
            String username
    ) {
        Optional<TelegramAccount> existingTelegram =
                telegramAccountRepository.findByTelegramUserId(telegramUserId);

        if (existingTelegram.isPresent()) {
            return LinkResult.ALREADY_LINKED;
        }

        Optional<Client> client = clientService.findByPhone(phone);

        if (client.isEmpty()) {
            return LinkResult.CLIENT_NOT_FOUND;
        }

        if (telegramAccountRepository.findByClientId(client.get().getId()).isPresent()) {
            return LinkResult.CLIENT_ALREADY_LINKED;
        }

        TelegramAccount account = new TelegramAccount(
                client.get().getId(),
                telegramUserId,
                telegramChatId,
                username,
                LocalDateTime.now()
        );

        telegramAccountRepository.save(account);

        return LinkResult.SUCCESS;
    }

    public enum LinkResult {
        SUCCESS,
        CLIENT_NOT_FOUND,
        ALREADY_LINKED,
        CLIENT_ALREADY_LINKED
    }
}