package com.example.tracker.repository;

import java.sql.Date;
import java.time.LocalDate;

public final class Sql {
    private Sql() {
    }

    public static Date date(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Date.valueOf(LocalDate.parse(value));
    }
}
