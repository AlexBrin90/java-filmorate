package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film uFilm) {
        return filmStorage.update(uFilm);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        return filmStorage.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<String> addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(userId, id);
        return ResponseEntity.ok("Лайк поставлен");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<String> deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(userId, id);
        return ResponseEntity.ok("Лайк убран");
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopFilms(count);
    }
}
