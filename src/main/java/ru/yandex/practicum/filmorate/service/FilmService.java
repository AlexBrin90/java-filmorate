package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorStorage directorStorage;
    private final FeedService feedService;


    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       GenreStorage genreStorage, MpaStorage mpaStorage, DirectorStorage directorStorage,
                       FeedStorage feedStorage, FeedService feedService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorStorage = directorStorage;
        this.feedService = feedService;
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        validateReferences(film);
        return filmStorage.create(film);
    }

    public void delete(Long filmId) {
        filmStorage.deleteFilm(filmId);
    }

    public Film update(Film film) {
        validateReferences(film);
        return filmStorage.update(film);
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    @Transactional
    public void addLike(Long userId, Long filmId) {
        userStorage.getById(userId);
        filmStorage.getById(filmId);
        filmStorage.addLike(filmId, userId);

        feedService.addEvent(
                userId,
                EventType.LIKE,
                Operation.ADD,
                filmId
        );
    }

    @Transactional
    public void deleteLike(Long userId, Long filmId) {
        userStorage.getById(userId);
        filmStorage.getById(filmId);
        filmStorage.deleteLike(filmId, userId);

        feedService.addEvent(
                userId,
                EventType.LIKE,
                Operation.REMOVE,
                filmId
        );
    }

    public List<Film> getPopFilms(int count, Integer genreId, Integer year) {
        if (genreId != null) {
            genreStorage.getById(genreId);
        }
        if (year != null && year < 1895) {
            throw new ConditionsNotMetException("Год не может быть меньше 1895");
        }
        return filmStorage.getPopular(count, genreId, year);
    }

    public List<Film> getByDirector(Long directorId, String sortBy) {
        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new ConditionsNotMetException("Сортировка должна быть year или likes");
        }
        directorStorage.getById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + directorId + " не найден"));
        return filmStorage.getByDirector(directorId, sortBy);
    }

    private void validateReferences(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            film.setMpa(mpaStorage.getById(film.getMpa().getId()));
        }
        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream()
                    .filter(genre -> genre != null && genre.getId() != null)
                    .map(genre -> genreStorage.getById(genre.getId()))
                    .distinct()
                    .sorted(java.util.Comparator.comparing(Genre::getId))
                    .toList());
        }
        validateDirectors(film);
    }

    private void validateDirectors(Film film) {
        Set<Long> ids = new TreeSet<>();
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                if (director == null || director.getId() == null) {
                    throw new ConditionsNotMetException("Для режиссёра фильма нужно указать id");
                }
                ids.add(director.getId());
            }
        }
        List<Director> directors = directorStorage.getByIds(ids);
        for (Director director : directors) {
            ids.remove(director.getId());
        }
        if (!ids.isEmpty()) {
            throw new NotFoundException("Режиссёр с id = " + ids.iterator().next() + " не найден");
        }
        film.setDirectors(directors);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя сравнивать пользователя с самим собой");
        }
        userStorage.getById(userId);
        userStorage.getById(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> search(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ConditionsNotMetException("Текст поиска не может быть пустым");
        }

        if (by == null || by.isBlank()) {
            throw new ConditionsNotMetException("Поле поиска не может быть пустым");
        }

        Set<String> fields = new HashSet<>();
        for (String field : by.split(",", -1)) {
            String normalize = field.trim().toLowerCase();
            if (normalize.isEmpty()) {
                throw new ConditionsNotMetException("Поле поиска содержит пустой элемент");
            }

            if (!"title".equals(normalize) && !"director".equals(normalize)) {
                throw new ConditionsNotMetException("Поле должно быть title или director");
            }
            fields.add(normalize);
        }

        return filmStorage.search(query.trim(), fields.contains("title"), fields.contains("director"));
    }
}
