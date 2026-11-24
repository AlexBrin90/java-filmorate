package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FeedRowMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.List;

@Repository
public class FeedDbStorage implements FeedStorage {

    private static final String INSERT_EVENT_QUERY = """
            INSERT INTO feed_events
            (timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?)
            """;


    private static final String FIND_FEED_QUERY = """
        SELECT fe.event_id, fe.timestamp, fe.user_id,
               fe.event_type, fe.operation, fe.entity_id
        FROM feed_events fe
        WHERE fe.user_id = ?
        OR (
            fe.event_type IN ('LIKE', 'REVIEW')
            AND fe.user_id IN (
                SELECT friend_id
                FROM friendships
                WHERE user_id = ?
            )
        )
        ORDER BY fe.timestamp ASC, fe.event_id ASC
        """;

    private final JdbcTemplate jdbc;
    private final FeedRowMapper feedMapper;

    public FeedDbStorage(JdbcTemplate jdbc, FeedRowMapper feedMapper) {
        this.jdbc = jdbc;
        this.feedMapper = feedMapper;
    }

    @Override
    public void addEvent(FeedEvent event) {
        jdbc.update(
                INSERT_EVENT_QUERY,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );
    }

    @Override
    public List<FeedEvent> getFeed(Long userId) {
        return jdbc.query(FIND_FEED_QUERY, feedMapper, userId, userId);
    }
}
