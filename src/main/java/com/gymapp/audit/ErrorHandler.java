package com.gymapp.audit;

import com.gymapp.ui.common.DialogService;

public final class ErrorHandler {

    private ErrorHandler() {
    }

    public static void handle(
            String source,
            String userMessage,
            Throwable throwable
    ) {
        ErrorLogger.log(source, throwable);

        DialogService.showInfo(
                "Помилка",
                userMessage
        );
    }
}