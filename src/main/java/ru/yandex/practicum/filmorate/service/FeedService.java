package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


import java.util.List;

@Service
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public FeedService(FeedStorage feedStorage, UserStorage userStorage) {
        this.feedStorage = feedStorage;
        this.userStorage = userStorage;
    }

    @Transactional
    public void addEvent(Long userId, EventType eventType,
                         Operation operation, Long entityId) {
        FeedEvent event = new FeedEvent();
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);

        feedStorage.addEvent(event);
    }

    @Transactional(readOnly = true)
    public List<FeedEvent> getFeed(Long userId) {
        if (userId == null || userId <= 0) {
            throw new NotFoundException(
                    "Пользователь с id " + userId + " не найден"
            );
        }

        userStorage.getById(userId);

        return feedStorage.getFeed(userId);
    }
}