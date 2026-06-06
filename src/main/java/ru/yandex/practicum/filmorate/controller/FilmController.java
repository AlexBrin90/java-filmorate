package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    public static final LocalDate DATE_OF_BIRTH_CINEMA = LocalDate.of(1895, 12, 28);

    Map<Long, Film> films = new HashMap<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy 'года'", new Locale("ru", "RU"));

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Добавляем новый фильм {}", film.getName());

        if (film.getReleaseDate().isBefore(DATE_OF_BIRTH_CINEMA)) {
            String errMess = "Дата релиза фильма не может быть раньше " + DATE_OF_BIRTH_CINEMA.format(fmt);
            log.warn(errMess);
            throw new ValidationException(errMess);
        }
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

    @PutMapping
    public Film update(@Valid @RequestBody Film uFilm) {

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

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
