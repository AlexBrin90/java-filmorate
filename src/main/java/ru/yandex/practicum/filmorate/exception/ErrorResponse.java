package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ErrorResponse {
    private final int status;
    private final List<FieldError> errors;

    public ErrorResponse(int status, List<FieldError> errors) {
        this.status = status;
        this.errors = errors;
    }

    public record FieldError(String field, String message) {
    }
}
