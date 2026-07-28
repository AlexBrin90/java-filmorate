package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> getAll() {
        return films.values();
    }

    public Film create(Film film) {
        log.info("Добавляем новый фильм {}", film.getName());

        if (film.getDuration() < 0) {
            String errMess = "Продолжительность фильма не может быть меньше 0 минут";
            log.warn(errMess);
            throw new ValidationException(errMess);
            //Фильмы, длительностью до 1 минуты существуют - https://vkvideo.ru/@club8495133 - да, это прям фильмы))))
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен. ID {}", film.getName(), film.getId());
        return film;
    }

    public Film update(Film uFilm) {
        log.info("Начинаем обновлять информацию о фильме:");

        if (uFilm.getId() == null) {
            String errMess = "ID должен быть указан";
            log.warn(errMess);
            throw new ConditionsNotMetException(errMess);
        }

        if (films.containsKey(uFilm.getId())) {
            Film oFilm = films.get(uFilm.getId());
            log.info("Обновляем данные о фильме {}", oFilm.getName());
            Optional.ofNullable(uFilm.getDescription()).ifPresent(oFilm::setDescription);
            Optional.of(uFilm.getName()).ifPresent(oFilm::setName);
            Optional.ofNullable(uFilm.getDuration()).ifPresent(oFilm::setDuration);
            Optional.ofNullable(uFilm.getReleaseDate()).ifPresent(oFilm::setReleaseDate);
            log.info("Данные о фильме успешно обновлены {}", oFilm);
            return oFilm;
        }
        String errMess = "Фильм с id = " + uFilm.getId() + " не найден";
        log.warn(errMess);
        throw new NotFoundException(errMess);
    }

    public Film getById(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return films.get(id);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
