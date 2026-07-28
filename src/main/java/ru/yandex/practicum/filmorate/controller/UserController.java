package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User updUser) {
        return userStorage.update(updUser);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userStorage.getById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<String> addToFriends(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addToFriends(id, friendId);
        return ResponseEntity.ok("Пользователь добавлен в друзья");
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<String> deleteFromFriends(@PathVariable Long id, @PathVariable Long friendId) {
        userService.deleteFromFriends(id, friendId);
        return ResponseEntity.ok("Пользователь удален из друзей");
    }

    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable Long id) {
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getMutualFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getMutualFriends(id, otherId);
    }
}
