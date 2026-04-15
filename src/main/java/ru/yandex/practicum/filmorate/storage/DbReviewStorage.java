package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("DbReviewStorage")
@Slf4j
public class DbReviewStorage extends DbBaseStorage<Review> implements ReviewStorage {

    private static final String GET_BY_ID_QUERY = "SELECT reviews.*, 0 AS useful FROM reviews WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?";
    private static final String GET_REVIEWS_QUERY = "SELECT reviews.*, 0 AS useful FROM reviews ORDER BY useful LIMIT ?";
    private static final String GET_REVIEWS_BY_FILM_ID_QUERY = "SELECT reviews.*, 0 AS useful FROM reviews WHERE film_id = ? ORDER BY useful LIMIT ?";

    public DbReviewStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review getReviewById(long id) {
        Optional<Review> reviewOpt = findOne(GET_BY_ID_QUERY, id);
        if (reviewOpt.isEmpty()) {
            String errorMessage = String.format("Отзыв с id '%d' не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return reviewOpt.get();
    }

    @Override
    public Review addReview(Review newReview) {
        log.info("Начало добавления отзыва {}", newReview);
        Long id = insert(
                INSERT_QUERY,
                newReview.getContent(),
                newReview.getIsPositive(),
                newReview.getUserId(),
                newReview.getFilmId()
        );

        newReview.setReviewId(id);
        log.info("Добавлен новый отзыв: {}", newReview);
        return newReview;
    }

    @Override
    public void updateReview(Review newReview) {
        update(
                UPDATE_QUERY,
                newReview.getContent(),
                newReview.getIsPositive(),
                newReview.getUserId(),
                newReview.getFilmId(),
                newReview.getReviewId()
        );
    }

    @Override
    public void deleteReviewById(long id) {
        update(DELETE_QUERY, id);
    }

    @Override
    public Collection<Review> getReviews(Integer count) {
        return findMany(GET_REVIEWS_QUERY, count);
    }

    @Override
    public Collection<Review> getReviewsByFilmId(Long filmId, Integer count) {
        return findMany(GET_REVIEWS_BY_FILM_ID_QUERY, filmId, count);
    }
}
