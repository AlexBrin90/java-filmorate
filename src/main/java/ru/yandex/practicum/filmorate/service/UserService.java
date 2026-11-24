package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final FeedService feedService;

    public UserService(UserStorage userStorage, FeedService feedService) {
        this.userStorage = userStorage;
        this.feedService = feedService;
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void delete(Long userId) {
        userStorage.deleteUser(userId);
    }

    public User getById(Long id) {
        return userStorage.getById(id);
    }

    @Transactional
    public void addToFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавлять себя в друзья");
        }
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.addFriend(userId, friendId);

        feedService.addEvent(
                userId,
                EventType.FRIEND,
                Operation.ADD,
                friendId
        );
    }

    @Transactional
    public void deleteFromFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя удалить себя из друзей");
        }
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.deleteFriend(userId, friendId);

        feedService.addEvent(
                userId,
                EventType.FRIEND,
                Operation.REMOVE,
                friendId
        );
    }

    public List<User> getAllFriends(Long userId) {
        userStorage.getById(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getMutualFriends(Long userId, Long otherId) {
        userStorage.getById(userId);
        userStorage.getById(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }
}
