package com.gymapp.telegram.service;

import com.gymapp.client.db.Client;
import com.gymapp.client.service.ClientService;
import com.gymapp.telegram.bot.GymTelegramBot;
import com.gymapp.telegram.db.TelegramAccount;
import com.gymapp.telegram.db.TelegramAccountRepository;
import com.gymapp.telegram.dto.TelegramRecipient;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class TelegramMessagingService {

    private final TelegramAccountRepository telegramAccountRepository;
    private final ClientService clientService;
    private final GymTelegramBot telegramBot;

    public TelegramMessagingService(
            TelegramAccountRepository telegramAccountRepository,
            ClientService clientService,
            GymTelegramBot telegramBot
    ) {
        this.telegramAccountRepository = telegramAccountRepository;
        this.clientService = clientService;
        this.telegramBot = telegramBot;
    }

    public boolean isTelegramConnected(Long clientId) {
        return telegramAccountRepository
                .findByClientId(clientId)
                .isPresent();
    }

    public int getConnectedClientsCount() {
        return telegramAccountRepository.findAll().size();
    }

    public List<TelegramRecipient> getRecipients() {
        return telegramAccountRepository.findAll()
                .stream()
                .map(account ->
                        clientService.findById(account.getClientId())
                                .map(this::toRecipient)
                )
                .flatMap(Optional::stream)
                .sorted((first, second) ->
                        first.fullName()
                                .compareToIgnoreCase(second.fullName())
                )
                .toList();
    }

    public boolean sendToClient(
            Long clientId,
            String text
    ) {
        if (text == null || text.isBlank()) {
            return false;
        }

        Optional<TelegramAccount> account =
                telegramAccountRepository.findByClientId(clientId);

        if (account.isEmpty()) {
            return false;
        }

        return telegramBot.sendMessage(
                account.get().getTelegramChatId(),
                text.trim()
        );
    }

    public BroadcastResult sendToClients(
            Collection<Long> clientIds,
            String text
    ) {
        return sendToClients(
                clientIds,
                text,
                null
        );
    }

    public BroadcastResult sendToClients(
            Collection<Long> clientIds,
            String text,
            BiConsumer<Integer, Integer> progressCallback
    ) {
        if (clientIds == null
                || clientIds.isEmpty()
                || text == null
                || text.isBlank()) {

            return new BroadcastResult(0, 0);
        }

        int successful = 0;
        int failed = 0;
        int processed = 0;
        int total = clientIds.size();

        String normalizedText = text.trim();

        for (Long clientId : clientIds) {

            Optional<TelegramAccount> account =
                    telegramAccountRepository.findByClientId(clientId);

            if (account.isEmpty()) {
                failed++;
            } else {

                boolean sent = telegramBot.sendMessage(
                        account.get().getTelegramChatId(),
                        normalizedText
                );

                if (sent) {
                    successful++;
                } else {
                    failed++;
                }
            }

            processed++;

            notifyProgress(
                    progressCallback,
                    processed,
                    total
            );
        }

        return new BroadcastResult(
                successful,
                failed
        );
    }

    public BroadcastResult sendToAll(
            String text
    ) {
        return sendToAll(
                text,
                null
        );
    }

    public BroadcastResult sendToAll(
            String text,
            BiConsumer<Integer, Integer> progressCallback
    ) {
        if (text == null || text.isBlank()) {
            return new BroadcastResult(0, 0);
        }

        List<TelegramAccount> accounts =
                telegramAccountRepository.findAll();

        int successful = 0;
        int failed = 0;
        int processed = 0;
        int total = accounts.size();

        String normalizedText = text.trim();

        for (TelegramAccount account : accounts) {

            boolean sent = telegramBot.sendMessage(
                    account.getTelegramChatId(),
                    normalizedText
            );

            if (sent) {
                successful++;
            } else {
                failed++;
            }

            processed++;

            notifyProgress(
                    progressCallback,
                    processed,
                    total
            );
        }

        return new BroadcastResult(
                successful,
                failed
        );
    }

    private void notifyProgress(
            BiConsumer<Integer, Integer> progressCallback,
            int processed,
            int total
    ) {
        if (progressCallback != null) {
            progressCallback.accept(
                    processed,
                    total
            );
        }
    }

    private TelegramRecipient toRecipient(
            Client client
    ) {
        return new TelegramRecipient(
                client.getId(),
                client.getClientNumber(),
                client.getFullName()
        );
    }

    public record BroadcastResult(
            int successful,
            int failed
    ) {

        public int total() {
            return successful + failed;
        }
    }
}