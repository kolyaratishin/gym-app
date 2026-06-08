package com.gymapp.audit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ErrorLogger {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ErrorLogger() {
    }

    public static void log(Throwable throwable) {
        log("Unknown", throwable);
    }

    public static void log(String source, Throwable throwable) {
        try {
            Path logFile = LogFileResolver.errorLogFile();

            Files.createDirectories(logFile.getParent());

            String message = """

                    ==================================================
                    Time: %s
                    Source: %s
                    Type: %s
                    Message: %s

                    Stacktrace:
                    %s
                    """
                    .formatted(
                            LocalDateTime.now().format(TIME_FORMATTER),
                            source,
                            throwable.getClass().getName(),
                            throwable.getMessage(),
                            stackTraceToString(throwable)
                    );

            Files.writeString(
                    logFile,
                    message,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
            // логування не повинно ламати програму
        }
    }

    private static String stackTraceToString(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}