package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       GenreStorage genreStorage, MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film create(Film film) {
        validateReferences(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateReferences(film);
        return filmStorage.update(film);
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    public void addLike(Long userId, Long filmId) {
        userStorage.getById(userId);
        filmStorage.getById(filmId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long userId, Long filmId) {
        userStorage.getById(userId);
        filmStorage.getById(filmId);
        filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getPopFilms(int count) {
        return filmStorage.getPopular(count);
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
    }
}
