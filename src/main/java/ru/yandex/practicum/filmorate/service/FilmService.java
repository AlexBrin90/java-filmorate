package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage inMemoryFilmStorage;
    private final UserStorage inMemoryUserStorage;

    public FilmService(FilmStorage inMemoryFilmStorage, UserStorage inMemoryUserStorage) {
        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public void addLike(Long userId, Long filmId) {  // поставить лайк
        Film film = inMemoryFilmStorage.getById(filmId);

        if (film.getLikes().contains(userId)) {
            throw new ConditionsNotMetException("Пользователь уже поставил лайк фильму");
        }
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}",
                inMemoryUserStorage.getById(userId).getName(), inMemoryFilmStorage.getById(filmId).getName());
    }

    public void deleteLike(Long userId, Long filmId) { // удалить лайк
        Film film = inMemoryFilmStorage.getById(filmId);

        if (!film.getLikes().contains(userId)) {
            throw new ConditionsNotMetException("Пользователь не ставил лайк фильму");
        }
        film.getLikes().remove(userId);
        log.info("Пользователь {} убрал лайк у фильма {}",
                inMemoryUserStorage.getById(userId).getName(), inMemoryFilmStorage.getById(filmId).getName());
    }

    public List<Film> getPopFilms(int count) {  // вывод популярных фильмов по количеству лайков
        return inMemoryFilmStorage.getAll().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .toList();
    }
}
