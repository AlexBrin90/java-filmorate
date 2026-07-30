package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage inMemoryUserStorage;

    public UserService(UserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public void addToFriends(Long userId, Long friendId) {  // метод для добавления в друзья
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавлять себя в друзья");
        }
        User user = inMemoryUserStorage.getById(userId);
        User friend = inMemoryUserStorage.getById(friendId);

        if (user.getFriends().contains(friendId)) {
            log.info("Пользователи {} и {} уже являются друзьями", user.getName(), friend.getName());
            return;
        }
        user.getFriends().add(friendId);
        log.info("Пользователь {} добавлен в друзья пользователю {}", user.getName(), friend.getName());
        friend.getFriends().add(userId);
        log.info("Пользователь {} добавлен в друзья пользователю {}", friend.getName(), user.getName());
    }

    public void deleteFromFriends(Long userId, Long friendId) {  // метод для удаления из друзей
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя удалить себя из друзей");
        }
        User user = inMemoryUserStorage.getById(userId);
        User friend = inMemoryUserStorage.getById(friendId);

        if (!user.getFriends().contains(friendId)) {
            log.info("Пользователи {} и {} не являются друзьями", user.getName(), friend.getName());
            return;
        }
        user.getFriends().remove(friendId);
        log.info("Пользователь {} удален из друзей у пользователя {}", user.getName(), friend.getName());
        friend.getFriends().remove(userId);
        log.info("Пользователь {} удален из друзей у пользователя {}", friend.getName(), user.getName());
    }

    public List<User> getAllFriends(Long userId) {  // метод для получения всех друзей пользователя
        User user = inMemoryUserStorage.getById(userId);
        return user.getFriends().stream()
                .map(friendId -> inMemoryUserStorage.getById(friendId))
                .collect(Collectors.toList());
    }

    public List<User> getMutualFriends(Long userId, Long friendId) { // метод вывода общих друзей с пользователем
        if (userId.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя искать общих друзей у самого себя");
        }
        User user = inMemoryUserStorage.getById(userId);
        User friend = inMemoryUserStorage.getById(friendId);
        List<User> mutualFriends = user.getFriends().stream()
                .filter(friend.getFriends()::contains)
                .map(inMemoryUserStorage::getById)
                .toList();
        log.info("Общие друзья пользователей {} и {} = {}", user.getName(), friend.getName(), mutualFriends);
        return mutualFriends;
    }

}
