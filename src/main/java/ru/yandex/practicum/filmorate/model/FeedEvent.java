package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class FeedEvent {

    @Positive(message = "ID события должен быть положительным числом")
    private Long eventId;

    @NotNull(message = "Время события не может быть null")
    @PositiveOrZero(message = "Время события не может быть отрицательным")
    private Long timestamp;

    @NotNull(message = "ID пользователя не может быть null")
    @Positive(message = "ID пользователя должен быть положительным числом")
    private Long userId;

    @NotNull(message = "Тип события должен быть указан")
    private EventType eventType;

    @NotNull(message = "Операция над событием должна быть указана")
    private Operation operation;

    @NotNull(message = "ID связанной сущности не может быть null")
    @Positive(message = "ID связанной сущности должен быть положительным числом")
    private Long entityId;
}