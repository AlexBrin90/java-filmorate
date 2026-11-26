package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_FILM_BY_ID_QUERY = """
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
    private static final String SAVE_GENRE_QUERY = """
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
    private static final String FIND_GENRES_FOR_FILMS_QUERY = """
            SELECT fg.film_id, g.id, g.name
            FROM genres g
            JOIN film_genres fg
            ON fg.genre_id = g.id
            WHERE fg.film_id IN (%s)
            ORDER BY g.id
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
            WHERE (? IS NULL OR EXISTS (
            SELECT 1
            FROM film_genres AS fg
            WHERE fg.film_id = f.id AND fg.genre_id = ?
            )) AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?)
            GROUP BY f.id, m.name
            ORDER BY likes_count DESC, f.id LIMIT ?
            """;
    private static final String FIND_FILMS_BY_IDS_QUERY = """
            SELECT f.*, m.name AS mpa_name
            FROM films AS f
            JOIN mpa_ratings AS m
            ON m.id = f.mpa_id
            WHERE f.id IN (%s)
            ORDER BY f.id
            """;
    private static final String DELETE_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String SAVE_DIRECTOR_QUERY = """
            MERGE INTO film_directors (film_id, director_id)
            KEY(film_id, director_id) VALUES (?, ?)
            """;
    private static final String FIND_DIRECTORS_FOR_FILMS_QUERY = """
            SELECT fd.film_id, d.id, d.name
            FROM directors d
            JOIN film_directors fd ON fd.director_id = d.id
            WHERE fd.film_id IN (%s)
            ORDER BY d.id
            """;
    private static final String FIND_BY_DIRECTOR_YEAR_QUERY = """
            SELECT f.*, m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON m.id = f.mpa_id
            JOIN film_directors fd ON fd.film_id = f.id
            WHERE fd.director_id = ?
            ORDER BY EXTRACT(YEAR FROM f.release_date), f.id
            """;
    private static final String FIND_BY_DIRECTOR_LIKES_QUERY = """
            SELECT f.*, m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON m.id = f.mpa_id
            JOIN film_directors fd ON fd.film_id = f.id
            LEFT JOIN likes l ON l.film_id = f.id
            WHERE fd.director_id = ?
            GROUP BY f.id, m.name
            ORDER BY COUNT(l.user_id) DESC, f.id
            """;
    private static final String FIND_COMMON_FILMS_QUERY = """
            SELECT f.*, m.name AS mpa_name, COUNT(l3.user_id) AS likes_count
            FROM likes l1 JOIN films f ON f.id = l1.film_id
            JOIN mpa_ratings m ON m.id = f.mpa_id
            JOIN likes l2 ON l1.film_id = l2.film_id
            JOIN likes l3 ON l1.film_id = l3.film_id
            WHERE l1.user_id = ? AND l2.user_id = ?
            GROUP BY f.id, m.name
            ORDER BY likes_count DESC, l1.film_id
            """;

    private static final String FIND_BY_SEARCH_QUERY = """
            SELECT f.*, m.name AS mpa_name, COUNT(DISTINCT l.user_id) AS likes_count
            FROM films f
            JOIN mpa_ratings m
            ON m.id = f.mpa_id
            LEFT JOIN likes l
            ON l.film_id = f.id
            WHERE %s
            GROUP BY f.id, m.name
            ORDER BY likes_count DESC, f.id
            """;

    private static final String SEARCH_BY_TITLE_ADD_TO_QUERY = "LOWER(f.name) LIKE ? ESCAPE '!'";

    private static final String SEARCH_BY_DIRECTOR_ADD_TO_QUERY = """
            f.id IN (SELECT fd.film_id
                     FROM film_directors fd
                     JOIN directors d
                     ON d.id = fd.director_id
                     WHERE LOWER(d.name) LIKE ? ESCAPE '!')
            """;
    private static final String DELETE_FILM = """
            DELETE FROM films WHERE id = ?""";


    private final GenreRowMapper genreMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper filmMapper, GenreRowMapper genreMapper) {
        super(jdbc, filmMapper);
        this.genreMapper = genreMapper;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbc.query(FIND_COMMON_FILMS_QUERY, mapper, userId, friendId);
        loadGenres(films);
        loadDirectors(films);
        return films;
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
        saveDirectors(id, film.getDirectors());

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
        jdbc.update(DELETE_DIRECTORS_QUERY, film.getId());
        saveDirectors(film.getId(), film.getDirectors());
        return getById(film.getId());
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = findMany(FIND_ALL_FILMS_QUERY);
        loadGenres(films);
        loadDirectors(films);
        return films;
    }

    @Override
    public Film getById(Long id) {
        Film film = findOne(FIND_FILM_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
        loadGenres(film);
        loadDirectors(List.of(film));
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
    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        if (count < 0) {
            throw new ConditionsNotMetException("Количество фильмов не может быть отрицательным");
        }

        List<Film> films = jdbc.query(FIND_POPULAR_QUERY, mapper, genreId, genreId, year, year, count);
        loadGenres(films);
        loadDirectors(films);
        return films;
    }

    @Override
    public void deleteFilm(Long filmId) {
        if (filmId == null) {
            throw new ConditionsNotMetException("Для удаления нужно указать id фильма");
        }
        int rows = jdbc.update(DELETE_FILM, filmId);
        if (rows == 0) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    public List<Film> getByDirector(Long directorId, String sortBy) {
        String query = "likes".equals(sortBy) ? FIND_BY_DIRECTOR_LIKES_QUERY : FIND_BY_DIRECTOR_YEAR_QUERY;
        List<Film> films = findMany(query, directorId);
        loadGenres(films);
        loadDirectors(films);
        return films;
    }

    private void saveDirectors(Long filmId, List<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }
        List<Object[]> batchArgs = directors.stream()
                .map(Director::getId)
                .distinct()
                .sorted()
                .map(directorId -> new Object[]{filmId, directorId})
                .toList();
        jdbc.batchUpdate(SAVE_DIRECTOR_QUERY, batchArgs);
    }

    private void loadDirectors(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = String.format(FIND_DIRECTORS_FOR_FILMS_QUERY, placeholders);
        Map<Long, List<Director>> directorsByFilmId = new HashMap<>();
        jdbc.query(query, rs -> {
            Director director = new Director();
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
            directorsByFilmId.computeIfAbsent(rs.getLong("film_id"), key -> new ArrayList<>()).add(director);
        }, filmIds.toArray());
        for (Film film : films) {
            film.setDirectors(directorsByFilmId.getOrDefault(film.getId(), List.of()));
        }
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

        if (ids.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = ids.stream()
                .map(genreId -> new Object[]{filmId, genreId})
                .toList();

        jdbc.batchUpdate(SAVE_GENRE_QUERY, batchArgs);
    }

    private void loadGenres(Film film) {
        film.setGenres(jdbc.query(FIND_GENRES_QUERY, genreMapper, film.getId()));
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .filter(Objects::nonNull)
                .toList();

        if (filmIds.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = String.format(FIND_GENRES_FOR_FILMS_QUERY, placeholders);

        Map<Long, List<Genre>> genresByFilmId = new HashMap<>();
        jdbc.query(query, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            genresByFilmId.computeIfAbsent(filmId, key -> new ArrayList<>()).add(genre);
        }, filmIds.toArray());

        for (Film film : films) {
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), List.of()));
        }
    }

    @Override
    public List<Film> getByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Long> filmIds = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (filmIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = String.format(FIND_FILMS_BY_IDS_QUERY, placeholders);

        List<Film> films = findMany(query, filmIds.toArray());
        loadGenres(films);
        loadDirectors(films);
        return films;
    }

    @Override
    public List<Film> search(String query, boolean byTitle, boolean byDirector) {
        List<String> conditions = new ArrayList<>();

        if (byTitle) {
            conditions.add(SEARCH_BY_TITLE_ADD_TO_QUERY);
        }

        if (byDirector) {
            conditions.add(SEARCH_BY_DIRECTOR_ADD_TO_QUERY);
        }

        if (conditions.isEmpty()) {
            return List.of();
        }

        String sql = String.format(FIND_BY_SEARCH_QUERY, String.join(" OR ", conditions));
        String pattern = "%" + escapeLikePattern(query.toLowerCase()) + "%";
        List<Object> params = new ArrayList<>();

        for (int i = 0; i < conditions.size(); i++) {
            params.add(pattern);
        }

        List<Film> films = jdbc.query(sql, mapper, params.toArray());
        loadGenres(films);
        loadDirectors(films);
        return films;
    }

    private String escapeLikePattern(String value) {
        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}