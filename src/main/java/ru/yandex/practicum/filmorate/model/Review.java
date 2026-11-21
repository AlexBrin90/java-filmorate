package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Укажите, положительный ли отзыв")
    private Boolean isPositive;

    private Long userId;
    private Long filmId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long useful;
}
