package com.gymapp.audit;

public final class GlobalExceptionHandler {

    private GlobalExceptionHandler() {
    }

    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                ErrorLogger.log("Uncaught exception in thread: " + thread.getName(), throwable)
        );
    }
}