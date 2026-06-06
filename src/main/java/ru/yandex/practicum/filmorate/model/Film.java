package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;


@Data
public class Film {

    private Long id;

    @NotNull
    @NotBlank(message = "WARN: Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "WARN: Описание не должно превышать 200 символов")
    @NotBlank(message = "WARN: Поле не может содержать только пробелы или быть пустым")
    private String description;

    private LocalDate releaseDate;

    private int duration;
}
