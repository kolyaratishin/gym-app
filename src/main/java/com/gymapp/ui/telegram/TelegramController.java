package com.gymapp.ui.telegram;

import com.gymapp.context.AppContext;
import com.gymapp.telegram.dto.TelegramRecipient;
import com.gymapp.telegram.service.TelegramMessagingService;
import com.gymapp.telegram.service.TelegramMessagingService.BroadcastResult;
import com.gymapp.ui.common.DialogService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramController {

    private final TelegramMessagingService telegramMessagingService;

    private List<TelegramRecipient> allRecipients;

    private final Map<Long, BooleanProperty> selectedRecipients =
            new HashMap<>();

    @FXML
    private Label connectedClientsLabel;

    @FXML
    private Label charactersLabel;

    @FXML
    private Label selectedCountLabel;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<TelegramRecipient> recipientsListView;

    @FXML
    private RadioButton allRecipientsRadio;

    @FXML
    private RadioButton selectedRecipientsRadio;

    @FXML
    private Button sendButton;

    public TelegramController() {
        this.telegramMessagingService =
                AppContext.telegramMessagingService();
    }

    @FXML
    private void initialize() {
        allRecipients = telegramMessagingService.getRecipients();

        connectedClientsLabel.setText(
                String.valueOf(allRecipients.size())
        );

        initializeSelections();
        configureRecipientsList();

        messageTextArea.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        charactersLabel.setText(
                                newValue.length() + " символів"
                        )
        );

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        refreshRecipientsList()
        );

        sendButton.setDisable(allRecipients.isEmpty());
    }

    private void initializeSelections() {
        for (TelegramRecipient recipient : allRecipients) {
            BooleanProperty selected =
                    new SimpleBooleanProperty(false);

            selected.addListener(
                    (observable, oldValue, newValue) ->
                            updateSelectedCount()
            );

            selectedRecipients.put(
                    recipient.clientId(),
                    selected
            );
        }
    }

    private void configureRecipientsList() {
        recipientsListView.setCellFactory(
                CheckBoxListCell.forListView(
                        recipient ->
                                selectedRecipients.get(
                                        recipient.clientId()
                                ),
                        new StringConverter<>() {

                            @Override
                            public String toString(
                                    TelegramRecipient recipient
                            ) {
                                return "№"
                                        + recipient.clientNumber()
                                        + "  "
                                        + recipient.fullName();
                            }

                            @Override
                            public TelegramRecipient fromString(
                                    String string
                            ) {
                                return null;
                            }
                        }
                )
        );

        refreshRecipientsList();
    }

    @FXML
    private void onRecipientModeChanged() {
        boolean selecting =
                selectedRecipientsRadio.isSelected();

        searchField.setDisable(!selecting);
        recipientsListView.setDisable(!selecting);

        selectedCountLabel.setVisible(selecting);
        selectedCountLabel.setManaged(selecting);

        updateSelectedCount();
    }

    private void refreshRecipientsList() {
        String query = searchField.getText();

        if (query == null) {
            query = "";
        }

        String normalized = query.trim().toLowerCase();

        List<TelegramRecipient> filtered =
                allRecipients.stream()
                        .filter(recipient ->
                                matches(recipient, normalized)
                        )
                        .toList();

        recipientsListView.getItems().setAll(filtered);
    }

    private boolean matches(
            TelegramRecipient recipient,
            String query
    ) {
        if (query.isBlank()) {
            return true;
        }

        return recipient.fullName()
                .toLowerCase()
                .contains(query)
                || String.valueOf(recipient.clientNumber())
                .contains(query);
    }

    private void updateSelectedCount() {
        long count = selectedRecipients.values()
                .stream()
                .filter(BooleanProperty::get)
                .count();

        selectedCountLabel.setText(
                "Вибрано: " + count
        );
    }

    @FXML
    private void onSend() {
        String text = messageTextArea.getText();

        if (text == null || text.isBlank()) {
            DialogService.showInfo(
                    "Telegram",
                    "Введіть текст повідомлення."
            );
            return;
        }

        if (allRecipientsRadio.isSelected()) {
            sendToAll(text);
        } else {
            sendToSelected(text);
        }
    }

    private void sendToAll(String text) {
        int recipients = allRecipients.size();

        if (recipients == 0) {
            DialogService.showInfo(
                    "Telegram",
                    "Немає клієнтів із підключеним Telegram."
            );
            return;
        }

        boolean confirmed = DialogService.showConfirm(
                "Масова розсилка",
                "Надіслати повідомлення "
                        + recipients
                        + " клієнтам?"
        );

        if (!confirmed) {
            return;
        }

        Task<BroadcastResult> task = new Task<>() {
            @Override
            protected BroadcastResult call() {
                return telegramMessagingService.sendToAll(
                        text,
                        (processed, total) ->
                                updateProgress(processed, total)
                );
            }
        };

        runSendingTask(task, recipients);
    }

    private void sendToSelected(String text) {
        List<Long> clientIds =
                selectedRecipients.entrySet()
                        .stream()
                        .filter(entry ->
                                entry.getValue().get()
                        )
                        .map(Map.Entry::getKey)
                        .toList();

        if (clientIds.isEmpty()) {
            DialogService.showInfo(
                    "Telegram",
                    "Виберіть хоча б одного клієнта."
            );
            return;
        }

        boolean confirmed = DialogService.showConfirm(
                "Telegram",
                "Надіслати повідомлення "
                        + clientIds.size()
                        + " клієнтам?"
        );

        if (!confirmed) {
            return;
        }

        Task<BroadcastResult> task = new Task<>() {
            @Override
            protected BroadcastResult call() {
                return telegramMessagingService.sendToClients(
                        clientIds,
                        text,
                        (processed, total) ->
                                updateProgress(processed, total)
                );
            }
        };

        runSendingTask(task, clientIds.size());
    }

    private void runSendingTask(
            Task<BroadcastResult> task,
            int total
    ) {
        sendButton.setDisable(true);
        sendButton.setText(
                "Надіслано: 0 / " + total
        );

        task.progressProperty().addListener(
                (observable, oldValue, newValue) -> {
                    double progress =
                            newValue.doubleValue();

                    if (progress < 0) {
                        return;
                    }

                    int processed =
                            (int) Math.round(progress * total);

                    sendButton.setText(
                            "Надіслано: "
                                    + processed
                                    + " / "
                                    + total
                    );
                }
        );

        task.setOnSucceeded(event -> {
            restoreSendButton();

            BroadcastResult result =
                    task.getValue();

            showResult(result);
        });

        task.setOnFailed(event -> {
            restoreSendButton();

            DialogService.showInfo(
                    "Telegram",
                    "Не вдалося виконати розсилку."
            );

            task.getException().printStackTrace();
        });

        Thread thread = new Thread(
                task,
                "telegram-sender"
        );

        thread.setDaemon(true);
        thread.start();
    }

    private void restoreSendButton() {
        sendButton.setDisable(
                allRecipients.isEmpty()
        );

        sendButton.setText("Надіслати");
    }

    private void showResult(
            BroadcastResult result
    ) {
        if (result.failed() == 0) {
            DialogService.showInfo(
                    "Telegram",
                    "Повідомлення успішно надіслано: "
                            + result.successful()
            );

            messageTextArea.clear();
            return;
        }

        DialogService.showInfo(
                "Telegram",
                "Розсилку завершено.\n\n"
                        + "Успішно: "
                        + result.successful()
                        + "\nНе вдалося: "
                        + result.failed()
        );
    }
}