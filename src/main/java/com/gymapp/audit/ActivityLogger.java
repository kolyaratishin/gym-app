package com.gymapp.audit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ActivityLogger {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ActivityLogger() {
    }

    public static void log(AuditEventType type, String description) {
        try {
            Path logFile = LogFileResolver.activityLogFile();

            Files.createDirectories(logFile.getParent());

            String line = "%s | %s | %s%n".formatted(
                    LocalDateTime.now().format(TIME_FORMATTER),
                    type.name(),
                    description
            );

            Files.writeString(
                    logFile,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
            // логування не має ламати програму
        }
    }
}