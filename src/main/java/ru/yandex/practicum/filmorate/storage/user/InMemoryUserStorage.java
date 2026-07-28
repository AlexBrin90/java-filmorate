package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy 'года'", new Locale("ru", "RU"));
    @Getter
    private Map<Long, User> users = new HashMap<>();

    public Collection<User> getAll() {
        return users.values();
    }

    public User create(User user) {

        log.info("Добавляем нового пользователя {}", user.getName());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            String errMess = "Дата рождения не может быть позже " + LocalDate.now().format(fmt);
            log.warn(errMess);
            throw new ValidationException(errMess);
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно добавлен. ID {}", user.getName(), user.getId());
        return user;
    }

    public User update(User updUser) {

        if (updUser.getId() == null) {
            String errMess = "ID должен быть указан";
            log.warn(errMess);
            throw new ConditionsNotMetException(errMess);
        }
        if (users.containsKey(updUser.getId())) {
            log.info("Начинаем обновлять информацию о пользователе:");

            User oUser = users.get(updUser.getId());
            log.info("Обновляем данные о пользователе {}", oUser.getName());
            Optional.ofNullable(updUser.getBirthday()).ifPresent(oUser::setBirthday);
            Optional.ofNullable(updUser.getLogin()).ifPresent(oUser::setLogin);
            Optional.ofNullable(updUser.getEmail()).ifPresent(oUser::setEmail);

            if (updUser.getName() == null || updUser.getName().isBlank()) {
                oUser.setName(updUser.getLogin());
            } else {
                oUser.setName(updUser.getName());
            }
            log.info("Данные о пользователе успешно обновлены {}", oUser);
            //знаю, что личные данные нужно маскировать, но это учебный проект, думаю не критично

            return oUser;
        }
        String errMess = "Пользователь с id = " + updUser.getId() + " не найден";
        log.warn(errMess);
        throw new NotFoundException(errMess);
    }

    public User getById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
