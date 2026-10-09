package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Data
public class Film {

    public static final LocalDate DATE_OF_BIRTH_CINEMA = LocalDate.of(1895, 12, 28);

    private Long id;

    @NotNull
    @NotBlank(message = "WARN: Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "WARN: Описание не должно превышать 200 символов")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    @AssertTrue(message = "дата релиза не может быть раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        return releaseDate != null && !releaseDate.isBefore(DATE_OF_BIRTH_CINEMA);
    }

    @Valid
    private List<Genre> genres = new ArrayList<>();

    @Valid
    @NotNull
    private MpaRating mpa;
}
