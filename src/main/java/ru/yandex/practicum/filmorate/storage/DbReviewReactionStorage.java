package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.ReviewReaction;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewReactionRowMapper;

@Repository
@Slf4j
public class DbReviewReactionStorage extends DbBaseStorage<ReviewReaction> implements ReviewReactionStorage {

    private static final String PUT_REACTION_QUERY = "INSERT INTO review_reactions (review_id, user_id, is_useful) VALUES (?, ?, ?)";
    private static final String DELETE_REACTION_QUERY = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?";
    private static final String GET_REACTION_QUERY = "SELECT * FROM review_reactions WHERE review_id = ? AND user_id = ?";

    public DbReviewReactionStorage(JdbcTemplate jdbc, ReviewReactionRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public void putReaction(long reviewId, long userId, boolean isUseful) {
        log.info("Storage: начало добавления реакции isUseful={} отзыву {} от пользователя {}", isUseful, reviewId, userId);
        if (findOne(GET_REACTION_QUERY, reviewId, userId).isPresent()) {
            update(DELETE_REACTION_QUERY, reviewId, userId);
        }
        update(PUT_REACTION_QUERY, reviewId, userId, isUseful);
        log.info("Storage: добавлена реакция isUseful={} отзыву {} от пользователя {}", isUseful, reviewId, userId);
    }

    @Override
    public void deleteReaction(long reviewId, long userId) {
        if (findOne(GET_REACTION_QUERY, reviewId, userId).isEmpty()) {
            String message = String.format("Пользователь %d не оставлял лайк/дизлайк отзыву %d", userId, reviewId);
            log.info(message);
            throw new NotFoundException(message);
        }
        update(DELETE_REACTION_QUERY, reviewId, userId);
    }

}
