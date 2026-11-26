package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    private static final Logger log = LoggerFactory.getLogger(DirectorController.class);

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<Director> getAll() {
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable Long id) {
        return directorService.getById(id);
    }

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        Director newDirector = directorService.create(director);
        log.info("Режиссер {} c id = {} создан", newDirector.getName(), newDirector.getId());
        return newDirector;
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        Director updateDirector = directorService.update(director);
        log.info("Режиссер c id = {} обновлен", updateDirector.getId());
        return updateDirector;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        directorService.delete(id);
        log.info("Режиссер c id = {} удален", id);
    }
}






