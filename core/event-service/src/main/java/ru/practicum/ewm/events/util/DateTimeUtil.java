package ru.practicum.ewm.events.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime parseNullable(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDateTime.parse(value, FORMATTER);
    }
}
