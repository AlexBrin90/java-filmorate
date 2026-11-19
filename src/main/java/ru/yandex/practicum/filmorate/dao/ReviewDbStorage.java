package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {
    private static final String INSERT_REVIEW_QUERY = """
            INSERT INTO reviews (content, is_positive, user_id, film_id)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_REVIEW_QUERY = """
            UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?
            """;
    private static final String DELETE_REVIEW_QUERY = "DELETE FROM reviews WHERE id = ?";
    private static final String SELECT_REVIEWS = """
            SELECT r.id, r.content, r.is_positive, r.user_id, r.film_id, COALESCE(SUM(v.vote), 0) AS useful
            FROM reviews r
            LEFT JOIN review_votes v ON v.review_id = r.id
            """;
    private static final String POPULARITY_ORDER = """
            GROUP BY r.id
            ORDER BY useful DESC, r.id
            LIMIT ?
            """;
    private static final String FIND_REVIEW_QUERY = SELECT_REVIEWS + "WHERE r.id = ? GROUP BY r.id";
    private static final String FIND_ALL_REVIEWS_QUERY = SELECT_REVIEWS + POPULARITY_ORDER;
    private static final String FIND_FILM_REVIEWS_QUERY = SELECT_REVIEWS + "WHERE r.film_id = ?\n" + POPULARITY_ORDER;
    private static final String SAVE_VOTE_QUERY = """
            MERGE INTO review_votes (review_id, user_id, vote) KEY (review_id, user_id)
            VALUES (?, ?, ?)
            """;
    private static final String DELETE_VOTE_QUERY = """
            DELETE FROM review_votes
            WHERE review_id = ? AND user_id = ? AND vote = ?
            """;

    public ReviewDbStorage(JdbcTemplate jdbc, ReviewRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review create(Review review) {
        long id = insert(INSERT_REVIEW_QUERY, review.getContent(), review.getIsPositive(),
                review.getUserId(), review.getFilmId());
        review.setReviewId(id);
        review.setUseful(0);
        return review;
    }

    @Override
    public boolean update(Review review) {
        return update(UPDATE_REVIEW_QUERY, review.getContent(), review.getIsPositive(), review.getReviewId()) > 0;
    }

    @Override
    public boolean delete(Long reviewId) {
        return update(DELETE_REVIEW_QUERY, reviewId) > 0;
    }

    @Override
    public Optional<Review> getById(Long reviewId) {
        return findOne(FIND_REVIEW_QUERY, reviewId);
    }

    @Override
    public List<Review> getAll(Long filmId, int count) {
        return filmId == null ? findMany(FIND_ALL_REVIEWS_QUERY, count)
                : findMany(FIND_FILM_REVIEWS_QUERY, filmId, count);
    }

    @Override
    public void saveVote(Long reviewId, Long userId, boolean helpful) {
        update(SAVE_VOTE_QUERY, reviewId, userId, helpful ? 1 : -1);
    }

    @Override
    public void deleteLike(Long reviewId, Long userId) {
        update(DELETE_VOTE_QUERY, reviewId, userId, 1);
    }

    @Override
    public void deleteDislike(Long reviewId, Long userId) {
        update(DELETE_VOTE_QUERY, reviewId, userId, -1);
    }
}
