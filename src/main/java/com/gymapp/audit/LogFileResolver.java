package com.gymapp.audit;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

final class LogFileResolver {

    private static final Path LOG_DIR = Path.of("logs");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private LogFileResolver() {
    }

    static Path activityLogFile() {
        String date = LocalDate.now().format(DATE_FORMATTER);
        return LOG_DIR
                .resolve("activity")
                .resolve("activity-" + date + ".log");
    }

    static Path errorLogFile() {
        String date = LocalDate.now().format(DATE_FORMATTER);
        return LOG_DIR
                .resolve("error")
                .resolve("error-" + date + ".log");
    }
}