package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    public ReviewService(ReviewStorage reviewStorage, UserService userService, FilmService filmService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    public Review create(Review review) {
        if (review.getUserId() == null || review.getFilmId() == null) {
            throw new ConditionsNotMetException("Для отзыва нужно указать ID пользователя и фильма");
        }
        userService.getById(review.getUserId());
        filmService.getById(review.getFilmId());
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        Long reviewId = review.getReviewId();
        if (reviewId == null) {
            throw new ConditionsNotMetException("Для обновления нужно указать ID отзыва");
        }
        if (!reviewStorage.update(review)) {
            throw reviewNotFound(reviewId);
        }
        return getById(reviewId);
    }

    public void delete(Long reviewId) {
        if (!reviewStorage.delete(reviewId)) {
            throw reviewNotFound(reviewId);
        }
    }

    public Review getById(Long reviewId) {
        return reviewStorage.getById(reviewId).orElseThrow(() -> reviewNotFound(reviewId));
    }

    public List<Review> getAll(Long filmId, int count) {
        if (count < 0) {
            throw new ConditionsNotMetException("Количество отзывов не может быть отрицательным");
        }
        if (filmId != null) {
            filmService.getById(filmId);
        }
        return reviewStorage.getAll(filmId, count);
    }

    public void saveVote(Long reviewId, Long userId, boolean helpful) {
        validateVoteReferences(reviewId, userId);
        reviewStorage.saveVote(reviewId, userId, helpful);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateVoteReferences(reviewId, userId);
        reviewStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateVoteReferences(reviewId, userId);
        reviewStorage.deleteDislike(reviewId, userId);
    }

    private void validateVoteReferences(Long reviewId, Long userId) {
        getById(reviewId);
        userService.getById(userId);
    }

    private NotFoundException reviewNotFound(Long reviewId) {
        return new NotFoundException("Отзыв с id = " + reviewId + " не найден");
    }
}
