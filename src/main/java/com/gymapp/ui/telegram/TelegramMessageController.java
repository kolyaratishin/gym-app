package com.gymapp.ui.telegram;

import com.gymapp.client.db.Client;
import com.gymapp.context.AppContext;
import com.gymapp.telegram.service.TelegramMessagingService;
import com.gymapp.ui.common.DialogService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class TelegramMessageController {

    private final TelegramMessagingService telegramMessagingService;

    @FXML
    private Label clientNameLabel;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Button sendButton;

    private Client client;

    public TelegramMessageController() {
        this.telegramMessagingService =
                AppContext.telegramMessagingService();
    }

    public void setClient(Client client) {
        this.client = client;

        clientNameLabel.setText(
                client.getFirstName() + " " + client.getLastName()
        );
    }

    @FXML
    private void onSend() {
        if (client == null) {
            return;
        }

        String message = messageTextArea.getText();

        if (message == null || message.isBlank()) {
            DialogService.showInfo(
                    "Telegram",
                    "Введіть текст повідомлення."
            );
            return;
        }

        boolean sent = telegramMessagingService.sendToClient(
                client.getId(),
                message
        );

        if (!sent) {
            DialogService.showInfo(
                    "Telegram",
                    "Не вдалося надіслати повідомлення."
            );
            return;
        }

        DialogService.showInfo(
                "Telegram",
                "Повідомлення надіслано."
        );

        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage =
                (Stage) messageTextArea.getScene().getWindow();

        stage.close();
    }
}