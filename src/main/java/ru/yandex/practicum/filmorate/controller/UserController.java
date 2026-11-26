package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final RecommendationService recommendationService;
    private final FeedService feedService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, RecommendationService recommendationService, FeedService feedService) {
        this.userService = userService;
        this.recommendationService = recommendationService;
        this.feedService = feedService;
    }

    @GetMapping
    public Collection<User> getAll() {
        return userService.getAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        User newUser = userService.create(user);
        log.info("Пользователь с id = {} создан", newUser.getId());
        return newUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        User updateUser = userService.update(user);
        log.info("Пользователь с id = {} обновлен", updateUser.getId());
        return updateUser;
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        log.info("Пользователь с id = {} удален", userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addToFriends(id, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFromFriends(@PathVariable Long id, @PathVariable Long friendId) {
        userService.deleteFromFriends(id, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable Long id) {
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getMutualFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getMutualFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long id) {
        return recommendationService.getRecommendations(id);
    }

    @GetMapping("/{id}/feed")
    public List<FeedEvent> getFeed(@PathVariable Long id) {
        return feedService.getFeed(id);
    }
}
