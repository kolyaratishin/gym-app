package com.gymapp.audit.service;

import com.gymapp.audit.AuditEventType;
import com.gymapp.audit.dto.ActivityLogEntry;
import com.gymapp.audit.dto.ErrorLogEntry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogReaderService {

    private static final Path LOG_DIR = Path.of("logs");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String ERROR_SEPARATOR = "==================================================";

    public List<ActivityLogEntry> readActivityLogs(LocalDate date) {
        Path file = activityLogFile(date);

        if (!Files.exists(file)) {
            return List.of();
        }

        try {
            List<ActivityLogEntry> entries = Files.readAllLines(file)
                    .stream()
                    .filter(line -> !line.isBlank())
                    .map(this::parseActivityLine)
                    .toList();

            return reversed(entries);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read activity log file: " + file.toAbsolutePath(), e);
        }
    }

    public List<ErrorLogEntry> readErrorLogs(LocalDate date) {
        Path file = errorLogFile(date);

        if (!Files.exists(file)) {
            return List.of();
        }

        try {
            String content = Files.readString(file);
            String[] blocks = content.split(ERROR_SEPARATOR);

            List<ErrorLogEntry> entries = new ArrayList<>();

            for (String block : blocks) {
                String normalizedBlock = block.trim();

                if (!normalizedBlock.contains("Time:")) {
                    continue;
                }

                entries.add(parseErrorBlock(normalizedBlock));
            }

            return reversed(entries);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read error log file: " + file.toAbsolutePath(), e);
        }
    }

    public Path logsDirectory() {
        return LOG_DIR;
    }

    private ActivityLogEntry parseActivityLine(String line) {
        String[] parts = line.split("\\|", 3);

        String time = part(parts, 0);
        String eventType = part(parts, 1);
        String description = part(parts, 2);

        return new ActivityLogEntry(
                time,
                eventType,
                eventLabel(eventType),
                description
        );
    }

    private ErrorLogEntry parseErrorBlock(String block) {
        return new ErrorLogEntry(
                extractField(block, "Time:"),
                extractField(block, "Source:"),
                extractField(block, "Type:"),
                extractField(block, "Message:"),
                block
        );
    }

    private String extractField(String block, String fieldName) {
        return block.lines()
                .filter(line -> line.trim().startsWith(fieldName))
                .map(line -> line.trim().substring(fieldName.length()).trim())
                .findFirst()
                .orElse("");
    }

    private String eventLabel(String eventType) {
        try {
            return AuditEventType.valueOf(eventType).label();
        } catch (Exception e) {
            return eventType;
        }
    }

    private String part(String[] parts, int index) {
        if (index >= parts.length) {
            return "";
        }

        return parts[index].trim();
    }

    private <T> List<T> reversed(List<T> items) {
        List<T> copy = new ArrayList<>(items);
        Collections.reverse(copy);
        return copy;
    }

    private Path activityLogFile(LocalDate date) {
        return LOG_DIR
                .resolve("activity")
                .resolve("activity-" + date.format(FILE_DATE_FORMATTER) + ".log");
    }

    private Path errorLogFile(LocalDate date) {
        return LOG_DIR
                .resolve("error")
                .resolve("error-" + date.format(FILE_DATE_FORMATTER) + ".log");
    }
}
