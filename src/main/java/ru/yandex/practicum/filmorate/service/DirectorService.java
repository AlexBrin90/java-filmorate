package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public List<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director getById(Long id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + id + " не найден"));
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (director.getId() == null) {
            throw new ConditionsNotMetException("Для обновления нужно указать id режиссёра");
        }
        if (!directorStorage.update(director)) {
            throw new NotFoundException("Режиссёр с id = " + director.getId() + " не найден");
        }
        return director;
    }

    public void delete(Long id) {
        if (!directorStorage.delete(id)) {
            throw new NotFoundException("Режиссёр с id = " + id + " не найден");
        }
    }
}
