package com.medilab.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class LocalDateAdapter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Named("stringToLocalDate")
    public LocalDate stringToLocalDate(String date) {
        return date != null ? LocalDate.parse(date, FORMATTER) : null;
    }

    @Named("localDateToString")
    public String localDateToString(LocalDate date) {
        return date != null ? date.format(FORMATTER) : null;
    }
}
