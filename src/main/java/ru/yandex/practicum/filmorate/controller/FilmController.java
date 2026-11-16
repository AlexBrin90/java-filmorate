package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        return filmService.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Map<String, String>> addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(userId, id);
        return ResponseEntity.ok(Map.of("message", "Лайк поставлен"));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Map<String, String>> deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(userId, id);
        return ResponseEntity.ok(Map.of("message", "Лайк убран"));
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopFilms(count);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getByDirector(@PathVariable Long directorId,
                                   @RequestParam(defaultValue = "year") String sortBy) {
        return filmService.getByDirector(directorId, sortBy);
    }
}
