package com.gymapp.ui.settings;

import com.gymapp.config.AppConfigService;
import com.gymapp.context.AppContext;
import com.gymapp.telegram.service.TelegramTokenValidator;
import com.gymapp.ui.common.DialogService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SettingsController {

    private final AppConfigService appConfigService =
            AppContext.appConfigService();

    private final TelegramTokenValidator tokenValidator =
            AppContext.telegramTokenValidator();

    @FXML
    private PasswordField tokenPasswordField;

    @FXML
    private TextField tokenTextField;

    @FXML
    private Label statusIndicator;

    @FXML
    private Label statusLabel;

    @FXML
    private Label statusDescriptionLabel;

    @FXML
    private Button showTokenButton;

    @FXML
    private Button validateButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    private String validatedToken;

    @FXML
    public void initialize() {
        configureTokenFields();
        loadCurrentConfiguration();
    }

    private void configureTokenFields() {
        tokenPasswordField.textProperty().bindBidirectional(
                tokenTextField.textProperty()
        );

        tokenPasswordField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (validatedToken != null
                            && !validatedToken.equals(newValue)) {

                        validatedToken = null;
                        saveButton.setDisable(true);

                        setStatus(
                                StatusType.WARNING,
                                "Токен змінено",
                                "Перевірте новий токен перед збереженням."
                        );
                    }
                }
        );
    }

    private void loadCurrentConfiguration() {
        String token = appConfigService.getTelegramToken();

        if (token == null) {
            deleteButton.setDisable(true);

            setStatus(
                    StatusType.NEUTRAL,
                    "Telegram не налаштований",
                    "Додайте Bot Token та перевірте підключення."
            );

            return;
        }

        tokenPasswordField.setText(token);

        if (appConfigService.hasStoredTelegramToken()) {
            deleteButton.setDisable(false);

            setStatus(
                    StatusType.SUCCESS,
                    "Telegram налаштований",
                    "Token збережений у налаштуваннях Gym App."
            );

            return;
        }

        if (appConfigService.isTelegramTokenFromEnvironment()) {
            deleteButton.setDisable(true);

            setStatus(
                    StatusType.SUCCESS,
                    "Telegram налаштований",
                    "Token отримано з environment variable."
            );
        }
    }

    @FXML
    private void toggleTokenVisibility() {
        boolean showToken =
                !tokenTextField.isVisible();

        tokenTextField.setVisible(showToken);
        tokenTextField.setManaged(showToken);

        tokenPasswordField.setVisible(!showToken);
        tokenPasswordField.setManaged(!showToken);

        showTokenButton.setText(
                showToken
                        ? "Сховати"
                        : "Показати"
        );

        if (showToken) {
            tokenTextField.requestFocus();
            tokenTextField.positionCaret(
                    tokenTextField.getText().length()
            );
        } else {
            tokenPasswordField.requestFocus();
            tokenPasswordField.positionCaret(
                    tokenPasswordField.getText().length()
            );
        }
    }

    @FXML
    private void validateToken() {
        String token = getCurrentToken();

        if (token.isBlank()) {
            validatedToken = null;
            saveButton.setDisable(true);

            setStatus(
                    StatusType.ERROR,
                    "Token не введено",
                    "Введіть Telegram Bot Token."
            );

            return;
        }

        validatedToken = null;

        setValidationInProgress(true);

        setStatus(
                StatusType.LOADING,
                "Перевіряємо підключення...",
                "Очікуємо відповідь від Telegram."
        );

        Task<TelegramTokenValidator.ValidationResult> task =
                new Task<>() {

                    @Override
                    protected TelegramTokenValidator.ValidationResult call() {
                        return tokenValidator.validate(token);
                    }
                };

        task.setOnSucceeded(event -> {
            setValidationInProgress(false);

            TelegramTokenValidator.ValidationResult result =
                    task.getValue();

            if (!result.valid()) {
                validatedToken = null;
                saveButton.setDisable(true);

                setStatus(
                        StatusType.ERROR,
                        "Не вдалося підключитися",
                        result.errorMessage()
                );

                return;
            }

            /*
             * Важливо:
             * зберігаємо саме token, який був перевірений.
             */
            validatedToken = token;

            saveButton.setDisable(false);

            String username = result.botUsername();

            if (username != null
                    && !username.isBlank()) {

                setStatus(
                        StatusType.SUCCESS,
                        "Telegram підключено",
                        "Бот @" + username
                                + " успішно відповів."
                );

            } else {
                setStatus(
                        StatusType.SUCCESS,
                        "Telegram підключено",
                        "Token успішно перевірено."
                );
            }
        });

        task.setOnFailed(event -> {
            setValidationInProgress(false);

            validatedToken = null;
            saveButton.setDisable(true);

            setStatus(
                    StatusType.ERROR,
                    "Помилка перевірки",
                    "Не вдалося перевірити Telegram Bot Token."
            );
        });

        Thread thread = new Thread(
                task,
                "telegram-token-validation"
        );

        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void saveToken() {
        String currentToken = getCurrentToken();

        if (validatedToken == null
                || !validatedToken.equals(currentToken)) {

            saveButton.setDisable(true);

            setStatus(
                    StatusType.WARNING,
                    "Token потрібно перевірити",
                    "Натисніть «Перевірити підключення» перед збереженням."
            );

            return;
        }

        try {
            appConfigService.saveTelegramToken(
                    currentToken
            );

            /*
             * Token уже збережений.
             * Повторно натискати Save немає сенсу.
             */
            saveButton.setDisable(true);
            deleteButton.setDisable(false);

            setStatus(
                    StatusType.SUCCESS,
                    "Налаштування збережено",
                    "Перезапустіть Gym App, щоб активувати Telegram."
            );

            DialogService.showInfo(
                    "Telegram налаштовано",
                    """
                    Telegram Bot Token успішно збережено.

                    Перезапустіть Gym App, щоб активувати Telegram.
                    """
            );

        } catch (Exception e) {
            setStatus(
                    StatusType.ERROR,
                    "Помилка збереження",
                    "Не вдалося зберегти Telegram Bot Token."
            );
        }
    }

    @FXML
    private void deleteToken() {
        if (!appConfigService.hasStoredTelegramToken()) {
            return;
        }

        boolean confirmed =
                DialogService.showConfirm(
                        "Видалення Telegram token",
                        """
                        Ви дійсно хочете видалити Telegram Bot Token?

                        Telegram-бот та автоматичні повідомлення
                        будуть вимкнені після перезапуску програми.
                        """
                );

        if (!confirmed) {
            return;
        }

        try {
            appConfigService.deleteTelegramToken();

            validatedToken = null;

            tokenPasswordField.clear();

            saveButton.setDisable(true);
            deleteButton.setDisable(true);

            setStatus(
                    StatusType.NEUTRAL,
                    "Telegram не налаштований",
                    "Token видалено. Перезапустіть Gym App, щоб застосувати зміни."
            );

            DialogService.showInfo(
                    "Telegram вимкнено",
                    """
                    Telegram Bot Token видалено.

                    Перезапустіть Gym App, щоб застосувати зміни.
                    """
            );

        } catch (Exception e) {
            setStatus(
                    StatusType.ERROR,
                    "Помилка видалення",
                    "Не вдалося видалити Telegram Bot Token."
            );
        }
    }

    private String getCurrentToken() {
        String token =
                tokenPasswordField.getText();

        return token == null
                ? ""
                : token.trim();
    }

    private void setValidationInProgress(
            boolean inProgress
    ) {
        validateButton.setDisable(inProgress);

        /*
         * Під час перевірки Save завжди disabled.
         * Після успішної перевірки він активується окремо.
         */
        saveButton.setDisable(true);

        deleteButton.setDisable(inProgress);

        tokenPasswordField.setDisable(inProgress);
        tokenTextField.setDisable(inProgress);
        showTokenButton.setDisable(inProgress);
    }

    private void setStatus(
            StatusType type,
            String title,
            String description
    ) {
        statusLabel.setText(title);
        statusDescriptionLabel.setText(description);

        statusIndicator.getStyleClass().removeAll(
                "status-neutral",
                "status-success",
                "status-error",
                "status-warning",
                "status-loading"
        );

        String styleClass = switch (type) {
            case SUCCESS -> "status-success";
            case ERROR -> "status-error";
            case WARNING -> "status-warning";
            case LOADING -> "status-loading";
            case NEUTRAL -> "status-neutral";
        };

        statusIndicator
                .getStyleClass()
                .add(styleClass);
    }

    private enum StatusType {
        NEUTRAL,
        SUCCESS,
        ERROR,
        WARNING,
        LOADING
    }
}