package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MpaRating {
    @NotNull
    private Integer id;
    private String name;
}
