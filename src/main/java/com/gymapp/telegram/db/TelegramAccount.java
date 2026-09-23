package com.gymapp.telegram.db;

import java.time.LocalDateTime;

public class TelegramAccount {

    private Long clientId;
    private Long telegramUserId;
    private Long telegramChatId;
    private String username;
    private LocalDateTime linkedAt;

    public TelegramAccount() {
    }

    public TelegramAccount(
            Long clientId,
            Long telegramUserId,
            Long telegramChatId,
            String username,
            LocalDateTime linkedAt
    ) {
        this.clientId = clientId;
        this.telegramUserId = telegramUserId;
        this.telegramChatId = telegramChatId;
        this.username = username;
        this.linkedAt = linkedAt;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(LocalDateTime linkedAt) {
        this.linkedAt = linkedAt;
    }
}