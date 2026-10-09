package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
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

    public User getById(Long id) {
        return userStorage.getById(id);
    }

    public void addToFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавлять себя в друзья");
        }
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFromFriends(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя удалить себя из друзей");
        }
        userStorage.getById(userId);
        userStorage.getById(friendId);
        userStorage.deleteFriend(userId, friendId);
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
