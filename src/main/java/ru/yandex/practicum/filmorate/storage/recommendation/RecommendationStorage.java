package ru.yandex.practicum.filmorate.storage.recommendation;

import java.util.List;

public interface RecommendationStorage {

    List<Long> findSimilarUserIds(Long userId);

    List<Long> findRecommendedFilmIds(Long userId, List<Long> similarUserIds);
}
