package com.gymapp.telegram.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;

public class TelegramTokenValidator {

    public ValidationResult validate(String token) {
        if (token == null || token.isBlank()) {
            return ValidationResult.invalid(
                    "Токен не може бути порожнім"
            );
        }

        try {
            TelegramBot bot =
                    new TelegramBot(token.trim());

            GetMeResponse response =
                    bot.execute(new GetMe());

            if (!response.isOk()) {
                return ValidationResult.invalid(
                        "Telegram не прийняв цей токен"
                );
            }

            String username =
                    response.user() != null
                            ? response.user().username()
                            : null;

            return ValidationResult.valid(username);

        } catch (Exception e) {
            return ValidationResult.error(
                    "Не вдалося підключитися до Telegram"
            );
        }
    }

    public record ValidationResult(
            boolean valid,
            String botUsername,
            String errorMessage
    ) {

        public static ValidationResult valid(
                String botUsername
        ) {
            return new ValidationResult(
                    true,
                    botUsername,
                    null
            );
        }

        public static ValidationResult invalid(
                String message
        ) {
            return new ValidationResult(
                    false,
                    null,
                    message
            );
        }

        public static ValidationResult error(
                String message
        ) {
            return new ValidationResult(
                    false,
                    null,
                    message
            );
        }
    }
}