package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.mappers.EventRowMapper;

import java.util.Collection;

@Repository
public class DbEventStorage extends DbBaseStorage<Event> implements EventStorage {

    private static final String GET_FEED_QUERY =
            "SELECT * FROM feed_events WHERE user_id = ? ORDER BY event_id ASC";
    private static final String INSERT_EVENT_QUERY =
            "INSERT INTO feed_events (timestamp, user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?, ?)";

    public DbEventStorage(JdbcTemplate jdbc, EventRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Event> getFeed(long userId) {
        return findMany(GET_FEED_QUERY, userId);
    }

    @Override
    public void addEvent(long userId, String eventType, String operation, long entityId) {
        insert(
                INSERT_EVENT_QUERY,
                System.currentTimeMillis(),
                userId,
                eventType,
                operation,
                entityId
        );
    }
}
