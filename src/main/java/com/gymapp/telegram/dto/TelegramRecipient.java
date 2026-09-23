package com.gymapp.telegram.dto;

public record TelegramRecipient(
        Long clientId,
        Integer clientNumber,
        String fullName
) {
}