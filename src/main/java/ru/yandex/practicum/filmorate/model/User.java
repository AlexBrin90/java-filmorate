package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {

    private Long id;

    @Email
    @NotNull
    @NotBlank(message = "WARN: Email не может быть пустым")
    private String email;

    @Pattern(regexp = "\\S+", message = "WARN: Логин не может содержать пробелы")
    @NotNull
    @NotBlank
    private String login;

    private String name;

    @PastOrPresent(message = "WARN: Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
