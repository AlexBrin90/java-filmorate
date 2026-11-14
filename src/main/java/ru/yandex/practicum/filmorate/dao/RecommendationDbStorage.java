package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class RecommendationDbStorage implements RecommendationStorage {
    private static final String FIND_SIMILAR_USERS_QUERY = """
                                                           WITH counts AS (
                                                                SELECT l2.user_id AS user_id, COUNT(*) AS cnt
                                                                FROM likes AS l1
                                                                JOIN likes AS l2
                                                                ON l2.film_id = l1.film_id
                                                                WHERE l1.user_id = ?
                                                                AND l2.user_id != ?
                                                                GROUP BY l2.user_id)
                                                           SELECT user_id
                                                           FROM counts
                                                           WHERE cnt = (SELECT MAX(cnt)
                                                                        FROM counts)
                                                           ORDER BY user_id
                                                           """;
    private static final String FIND_RECOMMENDED_FILM_IDS_QUERY = """
                                                                  SELECT f.id
                                                                  FROM films AS f
                                                                  LEFT JOIN likes AS all_likes
                                                                  ON all_likes.film_id = f.id
                                                                  WHERE f.id IN (SELECT l.film_id
                                                                                 FROM likes AS l
                                                                                 WHERE l.user_id IN (%s))
                                                                  AND f.id NOT IN (SELECT l.film_id
                                                                                   FROM likes AS l
                                                                                   WHERE l.user_id = ?)
                                                                  GROUP BY f.id
                                                                  ORDER BY COUNT(all_likes.user_id) DESC, f.id
                                                                  """;

    private final JdbcTemplate jdbc;

    public RecommendationDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Long> findSimilarUserIds(Long userId) {
        return jdbc.queryForList(FIND_SIMILAR_USERS_QUERY, Long.class, userId, userId);
    }

    @Override
    public List<Long> findRecommendedFilmIds(Long userId, List<Long> similarUserIds) {
        if (similarUserIds == null || similarUserIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", Collections.nCopies(similarUserIds.size(), "?"));
        String query = String.format(FIND_RECOMMENDED_FILM_IDS_QUERY, placeholders);

        List<Object> params = new ArrayList<>(similarUserIds);
        params.add(userId);

        return jdbc.queryForList(query, Long.class, params.toArray());
    }
}
