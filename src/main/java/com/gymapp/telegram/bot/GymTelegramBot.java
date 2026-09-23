package com.gymapp.telegram.bot;

import com.gymapp.telegram.service.TelegramLinkService;
import com.gymapp.telegram.service.TelegramLinkService.LinkResult;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetMeResponse;

public class GymTelegramBot {

    private final TelegramBot bot;
    private final TelegramLinkService telegramLinkService;

    private boolean running;

    public GymTelegramBot(
            String token,
            TelegramLinkService telegramLinkService
    ) {
        this.bot = new TelegramBot(token);
        this.telegramLinkService = telegramLinkService;
    }

    public boolean start() {
        if (running) {
            return true;
        }

        if (!validateConnection()) {
            System.err.println(
                    "Telegram bot was not started. " +
                            "Check token or internet connection."
            );

            return false;
        }

        try {
            System.out.println(
                    "Telegram bot: starting polling..."
            );

            bot.setUpdatesListener(
                    updates -> {
                        System.out.println(
                                "Telegram bot: received "
                                        + updates.size()
                                        + " updates"
                        );

                        for (Update update : updates) {
                            try {
                                System.out.println(
                                        "Telegram update START: "
                                                + update.updateId()
                                );

                                handleUpdate(update);

                                System.out.println(
                                        "Telegram update DONE: "
                                                + update.updateId()
                                );

                            } catch (Exception e) {
                                System.err.println(
                                        "Error processing Telegram update: "
                                                + update.updateId()
                                );

                                e.printStackTrace();
                            }
                        }

                        return UpdatesListener.CONFIRMED_UPDATES_ALL;
                    },
                    exception -> {
                        System.err.println(
                                "Telegram polling error:"
                        );

                        exception.printStackTrace();
                    }
            );

            running = true;

            System.out.println(
                    "Telegram bot: polling started"
            );

            return true;

        } catch (Exception e) {
            running = false;

            System.err.println(
                    "Failed to start Telegram bot"
            );

            e.printStackTrace();

            return false;
        }
    }

    private boolean validateConnection() {
        try {
            GetMeResponse response =
                    bot.execute(new GetMe());

            if (!response.isOk()) {
                System.err.println(
                        "Telegram token validation failed"
                );

                return false;
            }

            if (response.user() != null) {
                System.out.println(
                        "Telegram bot authenticated: @"
                                + response.user().username()
                );
            }

            return true;

        } catch (Exception e) {
            System.err.println(
                    "Failed to connect to Telegram"
            );

            e.printStackTrace();

            return false;
        }
    }

    private void handleUpdate(Update update) {
        Message message = update.message();

        if (message == null) {
            return;
        }

        long chatId = message.chat().id();

        if (message.contact() != null) {
            handleContact(message);
            return;
        }

        if ("/start".equals(message.text())) {
            handleStart(chatId);
        }
    }

    private void handleStart(long chatId) {
        KeyboardButton sharePhoneButton =
                new KeyboardButton(
                        "📱 Поділитися номером телефону"
                ).requestContact(true);

        ReplyKeyboardMarkup keyboard =
                new ReplyKeyboardMarkup(sharePhoneButton)
                        .resizeKeyboard(true)
                        .oneTimeKeyboard(true);

        bot.execute(
                new SendMessage(
                        chatId,
                        """
                        Вітаємо!

                        Щоб підключити Telegram до вашого профілю спортзалу,
                        поділіться номером телефону.
                        """
                ).replyMarkup(keyboard)
        );
    }

    private void handleContact(Message message) {
        Contact contact = message.contact();
        long chatId = message.chat().id();

        if (contact.userId() == null
                || !contact.userId().equals(
                message.from().id()
        )) {

            bot.execute(
                    new SendMessage(
                            chatId,
                            "Будь ласка, надішліть саме свій номер телефону."
                    )
            );

            return;
        }

        LinkResult result =
                telegramLinkService.link(
                        contact.phoneNumber(),
                        message.from().id(),
                        chatId,
                        message.from().username()
                );

        String response = switch (result) {
            case SUCCESS ->
                    "Telegram успішно підключено до вашого профілю ✅";

            case CLIENT_NOT_FOUND ->
                    """
                    Не вдалося знайти ваш номер у базі спортзалу.

                    Зверніться, будь ласка, до адміністратора.
                    """;

            case ALREADY_LINKED ->
                    "Ваш Telegram уже підключений ✅";

            case CLIENT_ALREADY_LINKED ->
                    """
                    До цього профілю вже підключений Telegram.

                    Зверніться, будь ласка, до адміністратора.
                    """;
        };

        bot.execute(
                new SendMessage(chatId, response)
                        .replyMarkup(
                                new ReplyKeyboardRemove()
                        )
        );
    }

    public void stop() {
        if (!running) {
            return;
        }

        try {
            bot.removeGetUpdatesListener();
        } finally {
            running = false;
        }
    }

    public boolean sendMessage(
            Long chatId,
            String text
    ) {
        if (!running) {
            return false;
        }

        try {
            var response =
                    bot.execute(
                            new SendMessage(
                                    chatId,
                                    text
                            )
                    );

            return response.isOk();

        } catch (Exception e) {
            System.err.println(
                    "Failed to send Telegram message to chat "
                            + chatId
            );

            e.printStackTrace();

            return false;
        }
    }

    public boolean isRunning() {
        return running;
    }
}