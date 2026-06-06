package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmorateApplicationTests {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        assertTrue(validator.validate(film).isEmpty(),
                "Фильм корректный. Валидация пройдена");
    }

    @Test
    void testFilmInvalidName() {
        Film film = new Film();
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(120);
        film.setName(null);
        assertFalse(validator.validate(film).isEmpty(), "Null-name вызывает ошибку");
        film.setName("");
        assertFalse(validator.validate(film).isEmpty(), "Пустое name вызывает ошибку");
        film.setName("   ");
        assertFalse(validator.validate(film).isEmpty(), "name из пробелов вызывает ошибку");
    }

    @Test
    void testFilmDescriptionBoundaryCondition() {
        Film film = new Film();
        film.setName("Inception");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        film.setDescription("a".repeat(200));
        assertTrue(validator.validate(film).isEmpty(),
                "description длиной 200 символов должно проходить валидацию");

        film.setDescription("a".repeat(201));
        assertFalse(validator.validate(film).isEmpty(),
                "description длиной 201 символ не должно проходить валидацию");
    }

    @Test
    void testFilmCreateReleaseDateBeforeCinemaBirth() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Ancient Film");
        film.setDescription("Very old");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(90);
        assertThrows(ValidationException.class, () -> controller.create(film),
                "releaseDate раньше 28.12.1895 года - ValidationException");
    }

    @Test
    void testFilmCreateNegativeDuration() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Test");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(-5);

        assertThrows(ValidationException.class, () -> controller.create(film),
                "Отрицательная duration - ValidationException");
    }

    @Test
    void testFilmUpdateWithoutId() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Updated");
        film.setDescription("Updated description");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(120);

        assertThrows(ConditionsNotMetException.class, () -> controller.update(film),
                "Обновление без ID - ConditionsNotMetException");
    }

    @Test
    void testFilmUpdateNotFound() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setId(999L);
        film.setName("Updated");
        film.setDescription("Updated description");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(120);

        assertThrows(NotFoundException.class, () -> controller.update(film),
                "Обновление несуществующего фильма - NotFoundException");
    }

    @Test
    void testValidUser() {
        User user = new User();
        user.setEmail("user@domain.com");
        user.setLogin("userLogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertTrue(validator.validate(user).isEmpty(),
                "Пользователь корректный. Валидация пройдена");
    }

    @Test
    void testUserInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("userLogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertFalse(validator.validate(user).isEmpty(),
                "Некорректный email - ошибка валидации");
    }

    @Test
    void testUserLoginWithSpaces() {
        User user = new User();
        user.setEmail("user@domain.com");
        user.setLogin("login with spaces");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertFalse(validator.validate(user).isEmpty(),
                "login с пробелами - ошибку валидации");
    }

    @Test
    void testUserCreateNameFromLoginIfEmpty() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName(null);

        User created = controller.create(user);
        assertEquals("testlogin", created.getName(),
                "Если name пустое, оно должно браться из login");
    }

    @Test
    void testUserCreateBirthdayInFuture() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> controller.create(user),
                "birthday в будущем - ValidationException");
    }

    @Test
    void testUserUpdateNotFound() {
        UserController controller = new UserController();
        User user = new User();
        user.setId(999L);
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(NotFoundException.class, () -> controller.update(user),
                "Обновление несуществующего пользователя - NotFoundException");
    }
}