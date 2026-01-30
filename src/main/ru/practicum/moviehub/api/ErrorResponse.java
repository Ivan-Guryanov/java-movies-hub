package ru.practicum.moviehub.api;

import java.time.Year;
import java.util.List;
import java.util.stream.Stream;

public class ErrorResponse {

    private final String error;
    private final List<String> details;

    public ErrorResponse(String error, String title, int year) {
        int maxYear = Year.now().getValue() + 1;
        this.error = error;

        this.details = Stream.<String>builder()
                .add((title == null || title.isBlank()) ? "Поле с названием фильма не заполнено." : null)
                .add((title != null && title.length() > 100) ? "Название не должно превышать 100 символов." : null)
                .add((year < 1888) ? "Год должен быть больше или равен 1888." : null)
                .add((year > maxYear) ? "Год должен быть меньше или равен " + maxYear + "." : null)
                .build()
                .filter(java.util.Objects::nonNull)
                .toList();
    }

}