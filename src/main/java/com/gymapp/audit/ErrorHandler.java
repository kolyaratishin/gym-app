package com.gymapp.audit;

import com.gymapp.ui.common.DialogService;

public final class ErrorHandler {

    private static final String ERROR_TITLE = "Помилка";

    private ErrorHandler() {
    }

    public static void handle(String source, String userMessage, Throwable throwable) {
        ErrorLogger.log(source, throwable);
        showUserError(userMessage);
    }

    public static void handle(String source, String userMessage, String details, Throwable throwable) {
        ErrorLogger.log(source + " | " + details, throwable);
        showUserError(userMessage);
    }

    public static void logOnly(String source, Throwable throwable) {
        ErrorLogger.log(source, throwable);
    }

    public static void logOnly(String source, String details, Throwable throwable) {
        ErrorLogger.log(source + " | " + details, throwable);
    }

    private static void showUserError(String userMessage) {
        DialogService.showInfo(
                ERROR_TITLE,
                userMessage + UserErrorMessages.DETAILS_IN_ERROR_LOG
        );
    }
}
