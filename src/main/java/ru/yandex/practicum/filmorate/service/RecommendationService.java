package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final RecommendationStorage recommendationStorage;


    public RecommendationService(UserStorage userStorage, FilmStorage filmStorage,
                                 RecommendationStorage recommendationStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.recommendationStorage = recommendationStorage;
    }

    public List<Film> getRecommendations(Long userId) {
        userStorage.getById(userId);

        List<Long> similarUserIds = recommendationStorage.findSimilarUserIds(userId);

        if (similarUserIds.isEmpty()) {
            return List.of();
        }

        List<Long> filmIds = recommendationStorage.findRecommendedFilmIds(userId, similarUserIds);

        if (filmIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Film> filmsById = filmStorage.getByIds(filmIds).stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        return filmIds.stream()
                .map(filmsById::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
