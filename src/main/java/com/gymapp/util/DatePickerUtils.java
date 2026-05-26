package com.gymapp.util;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DatePickerUtils {

    public static final DateTimeFormatter UI_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private DatePickerUtils() {
    }

    public static void configure(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<>() {

            @Override
            public String toString(LocalDate date) {
                return format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }

                try {
                    return LocalDate.parse(text, UI_DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
    }

    public static String format(LocalDate date) {
        if (date == null) {
            return "-";
        }

        return UI_DATE_FORMATTER.format(date);
    }
}