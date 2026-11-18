package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    boolean update(Review review);

    boolean delete(Long reviewId);

    Optional<Review> getById(Long reviewId);

    List<Review> getAll(Long filmId, int count);

    void saveVote(Long reviewId, Long userId, boolean helpful);

    void deleteVote(Long reviewId, Long userId);

    void deleteDislike(Long reviewId, Long userId);
}
