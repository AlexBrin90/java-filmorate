package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private Long id;

    @Email
    private String email;

    @Pattern(regexp = "\\S+", message = "WARN: Логин не может содержать пробелы")
    @NotNull
    private String login;

    private String name;

    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();
}
