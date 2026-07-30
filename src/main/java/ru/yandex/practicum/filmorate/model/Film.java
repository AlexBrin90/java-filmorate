package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
public class Film {

    public static final LocalDate DATE_OF_BIRTH_CINEMA = LocalDate.of(1895, 12, 28);

    private Long id;

    @NotNull
    @NotBlank(message = "WARN: Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "WARN: Описание не должно превышать 200 символов")
    @NotBlank(message = "WARN: Поле не может содержать только пробелы или быть пустым")
    private String description;


    private LocalDate releaseDate;

    private int duration;

    @AssertTrue(message = "дата релиза не может быть раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        return releaseDate != null && !releaseDate.isBefore(DATE_OF_BIRTH_CINEMA);
    }

    private Set<Long> likes = new HashSet<>();

    private Set<Long> dislikes;
}
