package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Transactional
@Import({FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaDbStorage.class,
        FilmRowMapper.class, UserRowMapper.class, GenreRowMapper.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_genres");
        jdbc.update("DELETE FROM friendships");
        jdbc.update("DELETE FROM films");
        jdbc.update("DELETE FROM users");
    }

    @Test
    void createUserReturnsGeneratedIdAndFillsEmptyName() {
        User user = userStorage.create(user("first@example.com", "first"));

        assertTrue(user.getId() > 0);
        assertEquals(user.getId(), userStorage.getById(user.getId()).getId());
        assertEquals("first", user.getName());

        User withBlankName = userStorage.create(user("second@example.com", "second"));
        withBlankName.setName("");
        assertEquals("second", userStorage.update(withBlankName).getName());
    }

    @Test
    void getAllUsersIsOrderedById() {
        User second = userStorage.create(user("second@example.com", "second"));
        User first = userStorage.create(user("first@example.com", "first"));

        List<Long> ids = userStorage.getAll().stream().map(User::getId).toList();
        assertEquals(2, ids.size());
        assertEquals(second.getId(), ids.getFirst());
        assertEquals(first.getId(), ids.getLast());
    }

    @Test
    void updateUserChangesFields() {
        User user = userStorage.create(user("first@example.com", "first"));
        user.setName("Updated");
        user.setLogin("updated-login");
        user.setBirthday(LocalDate.of(1995, 5, 5));

        User updated = userStorage.update(user);

        assertEquals("Updated", updated.getName());
        assertEquals("updated-login", updated.getLogin());
        assertEquals(LocalDate.of(1995, 5, 5), updated.getBirthday());
        assertEquals(user.getId(), updated.getId());
    }

    @Test
    void updateUnknownOrMissingIdThrowsExpectedErrors() {
        User missing = user(missingId(), "missing@example.com", "missing");

        assertThrows(NotFoundException.class, () -> userStorage.update(missing));

        User withoutId = user("no-id@example.com", "no-id");
        withoutId.setId(null);
        assertThrows(ConditionsNotMetException.class, () -> userStorage.update(withoutId));
    }

    @Test
    void getUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getById(-1L));
    }

    @Test
    void friendshipsAreDirectionalAndMutualFriendsAreCalculated() {
        User first = userStorage.create(user("first@example.com", "first"));
        User second = userStorage.create(user("second@example.com", "second"));
        User third = userStorage.create(user("third@example.com", "third"));

        userStorage.addFriend(first.getId(), second.getId());
        assertEquals(List.of(second.getId()), ids(userStorage.getFriends(first.getId())));
        assertTrue(userStorage.getFriends(second.getId()).isEmpty());

        userStorage.addFriend(second.getId(), first.getId());
        userStorage.addFriend(first.getId(), third.getId());
        userStorage.addFriend(second.getId(), third.getId());
        assertEquals(List.of(third.getId()), ids(userStorage.getCommonFriends(first.getId(), second.getId())));
        assertEquals(List.of(third.getId()), ids(userStorage.getCommonFriends(second.getId(), first.getId())));

        userStorage.deleteFriend(first.getId(), second.getId());
        assertTrue(userStorage.getFriends(first.getId()).containsAll(List.of(third)));
        assertEquals(List.of(first.getId(), third.getId()), ids(userStorage.getFriends(second.getId())));

        userStorage.deleteFriend(first.getId(), second.getId());
        userStorage.deleteFriend(first.getId(), third.getId());
        userStorage.deleteFriend(second.getId(), third.getId());
        assertTrue(userStorage.getFriends(first.getId()).isEmpty());
        assertTrue(userStorage.getCommonFriends(first.getId(), second.getId()).isEmpty());
    }

    @Test
    void createFilmKeepsMpaSortsGenresAndReturnsGeneratedId() {
        Film film = filmStorage.create(film("first", List.of(genre(2), genre(1), genre(1))));

        Film found = filmStorage.getById(film.getId());
        assertEquals(film.getId(), found.getId());
        assertEquals("first", found.getName());
        assertEquals(3, found.getMpa().getId());
        assertEquals("PG-13", found.getMpa().getName());
        assertEquals(List.of(1, 2), genreIds(found.getGenres()));

        Film withoutGenres = filmStorage.create(film("second", List.of()));
        assertTrue(filmStorage.getById(withoutGenres.getId()).getGenres().isEmpty());
    }

    @Test
    void getAllFilmsIsOrderedByIdAndKeepsGenres() {
        Film second = filmStorage.create(film("second", List.of()));
        Film first = filmStorage.create(film("first", List.of(genre(1))));

        List<Film> films = List.copyOf(filmStorage.getAll());
        assertEquals(2, films.size());
        assertEquals(second.getId(), films.getFirst().getId());
        assertEquals(first.getId(), films.getLast().getId());
        assertEquals(List.of(1), genreIds(films.getLast().getGenres()));
    }

    @Test
    void updateFilmReplacesGenresAndMpa() {
        Film film = filmStorage.create(film("first", List.of(genre(2))));
        film.setName("Renamed");
        MpaRating mpa = new MpaRating();
        mpa.setId(5);
        film.setMpa(mpa);
        film.setGenres(List.of());

        Film updated = filmStorage.update(film);

        assertEquals("Renamed", updated.getName());
        assertEquals("NC-17", updated.getMpa().getName());
        assertTrue(updated.getGenres().isEmpty());
        assertTrue(filmStorage.getById(film.getId()).getGenres().isEmpty());
    }

    @Test
    void updateUnknownOrMissingFilmThrowsExpectedErrors() {
        Film missing = film(missingId(), "missing", List.of());
        assertThrows(NotFoundException.class, () -> filmStorage.update(missing));

        Film withoutId = film("no-id", List.of());
        withoutId.setId(null);
        assertThrows(ConditionsNotMetException.class, () -> filmStorage.update(withoutId));
    }

    @Test
    void getUnknownFilmThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.getById(-1L));
    }

    @Test
    void likesAreUniqueAndPopularFilmsAreOrderedByLikesThenId() {
        User firstUser = userStorage.create(user("viewer@example.com", "viewer"));
        User secondUser = userStorage.create(user("second@example.com", "second"));
        Film first = filmStorage.create(film("first", List.of()));
        Film second = filmStorage.create(film("second", List.of()));

        filmStorage.addLike(first.getId(), firstUser.getId());
        filmStorage.addLike(first.getId(), firstUser.getId());
        filmStorage.addLike(first.getId(), secondUser.getId());
        filmStorage.addLike(second.getId(), secondUser.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertEquals(2, popular.size());
        assertEquals(first.getId(), popular.getFirst().getId());
        assertEquals(second.getId(), popular.getLast().getId());
        assertEquals(first.getId(), filmStorage.getPopular(1).getFirst().getId());

        filmStorage.deleteLike(first.getId(), secondUser.getId());
        filmStorage.deleteLike(first.getId(), secondUser.getId());
        popular = filmStorage.getPopular(10);
        assertEquals(first.getId(), popular.getFirst().getId());
        assertEquals(second.getId(), popular.getLast().getId());

        filmStorage.deleteLike(first.getId(), firstUser.getId());
        assertEquals(second.getId(), filmStorage.getPopular(1).getFirst().getId());
        filmStorage.deleteLike(second.getId(), secondUser.getId());
        assertEquals(2, filmStorage.getPopular(10).size());
        assertEquals(first.getId(), filmStorage.getPopular(1).getFirst().getId());
    }

    @Test
    void popularWithEmptyCatalogIsEmptyAndNegativeCountThrows() {
        assertTrue(filmStorage.getPopular(10).isEmpty());
        assertThrows(ConditionsNotMetException.class, () -> filmStorage.getPopular(-1));
    }

    @Test
    void genreCatalogIsSortedAndComplete() {
        List<Genre> genres = genreStorage.getAll();

        assertEquals(6, genres.size());
        assertEquals(List.of(1, 2, 3, 4, 5, 6), genres.stream().map(Genre::getId).toList());
        assertEquals("Драма", genreStorage.getById(2).getName());
        assertThrows(NotFoundException.class, () -> genreStorage.getById(100));
    }

    @Test
    void mpaCatalogIsSortedAndComplete() {
        assertEquals(5, mpaStorage.getAll().size());
        assertEquals(List.of(1, 2, 3, 4, 5),
                mpaStorage.getAll().stream().map(MpaRating::getId).toList());
        assertEquals("NC-17", mpaStorage.getById(5).getName());
        assertThrows(NotFoundException.class, () -> mpaStorage.getById(100));
    }

    private long missingId() {
        return -1L;
    }

    private List<Long> ids(List<User> users) {
        return users.stream().map(User::getId).toList();
    }

    private List<Integer> genreIds(List<Genre> genres) {
        return genres.stream().map(Genre::getId).toList();
    }

    private User user(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private User user(Long id, String email, String login) {
        User user = user(email, login);
        user.setId(id);
        return user;
    }

    private Film film(Long id, String name, List<Genre> genres) {
        Film film = film(name, genres);
        film.setId(id);
        return film;
    }

    private Film film(String name, List<Genre> genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setGenres(genres);
        MpaRating mpa = new MpaRating();
        mpa.setId(3);
        film.setMpa(mpa);
        return film;
    }

    private Genre genre(int id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }
}
