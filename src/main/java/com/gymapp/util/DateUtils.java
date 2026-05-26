package com.gymapp.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private DateUtils() {
    }

    public static String format(LocalDate date) {
        if (date == null) {
            return "-";
        }

        return FORMATTER.format(date);
    }
}