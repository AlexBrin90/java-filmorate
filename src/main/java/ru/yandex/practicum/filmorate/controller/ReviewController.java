package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable Long id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public List<Review> getAll(@RequestParam(required = false) Long filmId,
                               @RequestParam(defaultValue = "10") int count) {
        return reviewService.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.saveVote(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.saveVote(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteVote(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewService.deleteDislike(id, userId);
    }
}
