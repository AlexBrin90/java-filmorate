package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_FILM_BY_ID_QUERY =  """
                                                         SELECT f.*, m.name AS mpa_name
                                                         FROM films f
                                                         JOIN mpa_ratings m
                                                         ON m.id = f.mpa_id
                                                         WHERE f.id = ?
                                                         """;
    private static final String FIND_ALL_FILMS_QUERY = """
                                                       SELECT f.*, m.name AS mpa_name
                                                       FROM films f
                                                       JOIN mpa_ratings m
                                                       ON m.id = f.mpa_id
                                                       ORDER BY f.id
                                                       """;
    private static final String INSERT_FILM_QUERY = """
                                               INSERT INTO films(name, description, release_date, duration, mpa_id)
                                               VALUES (?, ?, ?, ?, ?)
                                               """;
    private static final String UPDATE_FILM_QUERY = """
                                                UPDATE films SET name = ?, description = ?, release_date = ?,
                                                duration = ?, mpa_id = ?
                                               WHERE id = ?
                                               """;
    private static final String DELETE_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String SAVE_GENRE_QUERY =  """
                                                    MERGE INTO film_genres (film_id, genre_id)
                                                    KEY(film_id, genre_id)
                                                    VALUES (?, ?)
                                                    """;
    private static final String FIND_GENRES_QUERY = """
                                                    SELECT g.id, g.name
                                                    FROM genres g
                                                    JOIN film_genres fg
                                                    ON fg.genre_id = g.id
                                                    WHERE fg.film_id = ? ORDER BY g.id
                                                    """;
    private static final String ADD_LIKE_QUERY = """
                                                 MERGE INTO likes (film_id, user_id)
                                                 KEY(film_id, user_id)
                                                 VALUES (?, ?)
                                                 """;
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR_QUERY = """
                                                     SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count
                                                     FROM films f JOIN mpa_ratings m ON m.id = f.mpa_id
                                                     LEFT JOIN likes l ON l.film_id = f.id
                                                     GROUP BY f.id, m.name
                                                     ORDER BY likes_count DESC, f.id LIMIT ?
                                                     """;
    private final GenreRowMapper genreMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper filmMapper, GenreRowMapper genreMapper) {
        super(jdbc, filmMapper);
        this.genreMapper = genreMapper;
    }

    @Override
    public Film create(Film film) {
        long id = insert(INSERT_FILM_QUERY,
                         film.getName(),
                         film.getDescription(),
                         Date.valueOf(film.getReleaseDate()),
                         film.getDuration(),
                         film.getMpa().getId());

        saveGenres(id, film.getGenres());

        return getById(id);
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ConditionsNotMetException("Для обновления нужно указать id фильма");
        }
        int rows = update(UPDATE_FILM_QUERY,
                          film.getName(),
                          film.getDescription(),
                          Date.valueOf(film.getReleaseDate()),
                          film.getDuration(),
                          film.getMpa().getId(),
                          film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        jdbc.update(DELETE_GENRES_QUERY, film.getId());
        saveGenres(film.getId(), film.getGenres());
        return getById(film.getId());
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = findMany(FIND_ALL_FILMS_QUERY);
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public Film getById(Long id) {
        Film film = findOne(FIND_FILM_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
        loadGenres(film);
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        if (count < 0) {
            throw new ConditionsNotMetException("Количество фильмов не может быть отрицательным");
        }

        List<Film> films = jdbc.query(FIND_POPULAR_QUERY, mapper, count);
        films.forEach(this::loadGenres);
        return films;
    }

    private void saveGenres(Long filmId, List<Genre> genres) {
        if (genres == null) {
            return;
        }

        List<Integer> ids = genres.stream()
                .filter(Objects::nonNull)
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        for (Integer genreId : ids) {
            jdbc.update(SAVE_GENRE_QUERY, filmId, genreId);
        }
    }

    private void loadGenres(Film film) {
        film.setGenres(jdbc.query(FIND_GENRES_QUERY, genreMapper, film.getId()));
    }
}
