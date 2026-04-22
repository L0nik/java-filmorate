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

    private static final String GET_BY_ID_QUERY = "SELECT reviews.* FROM reviews WHERE id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reviews WHERE id = ?";
    private static final String GET_REVIEWS_QUERY =
            "SELECT reviews.* FROM reviews LIMIT ?";
    private static final String GET_REVIEWS_BY_FILM_ID_QUERY =
            "SELECT reviews.* FROM reviews WHERE film_id = ? LIMIT ?";
    private static final String COUNT_REVIEW_USEFULNESS_QUERY =
            "SELECT SUM(CASE WHEN is_useful THEN 1 ELSE -1 END) AS useful " +
                    " FROM review_reactions WHERE review_id = ?";

    public DbReviewStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review getReviewById(long id) {
        log.info("Storage: получение отзыва по id={}", id);
        Optional<Review> reviewOpt = findOne(GET_BY_ID_QUERY, id);
        if (reviewOpt.isEmpty()) {
            String errorMessage = String.format("Отзыв с id '%d' не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        Review review = reviewOpt.get();
        setReviewUsefulness(review);
        log.info("Storage: отзыв успешно получен по id={}", id);
        return review;
    }

    @Override
    public Review addReview(Review newReview) {
        log.info("Storage: начало добавления отзыва {}", newReview);
        Long id = insert(
                INSERT_QUERY,
                newReview.getContent(),
                newReview.getIsPositive(),
                newReview.getUserId(),
                newReview.getFilmId()
        );

        newReview.setReviewId(id);
        log.info("Storage: добавлен новый отзыв: {}", newReview);
        return newReview;
    }

    @Override
    public void updateReview(Review newReview) {
        log.info("Storage: начало обновления отзыва {}", newReview);
        update(
                UPDATE_QUERY,
                newReview.getContent(),
                newReview.getIsPositive(),
                newReview.getUserId(),
                newReview.getFilmId(),
                newReview.getReviewId()
        );
        log.info("Storage: успешно обновлен отзыв {}", newReview);
    }

    @Override
    public void deleteReviewById(long id) {
        log.info("Storage: начало удаления отзыва id={}", id);
        update(DELETE_QUERY, id);
        log.info("Storage: успешно удален отзыв id={}", id);
    }

    @Override
    public Collection<Review> getReviews(Integer count) {
        log.info("Storage: получение отзывов в количестве {} штук", count);
        Collection<Review> reviews = findMany(GET_REVIEWS_QUERY, count);
        reviews.forEach(this::setReviewUsefulness);
        return reviews;
    }

    @Override
    public Collection<Review> getReviewsByFilmId(Long filmId, Integer count) {
        log.info("Storage: получение отзывов к фильму {} в количестве {} штук", filmId, count);
        Collection<Review> reviews = findMany(GET_REVIEWS_BY_FILM_ID_QUERY, filmId, count);
        reviews.forEach(this::setReviewUsefulness);
        return reviews;
    }

    private int getReviewUsefulness(long reviewId) {
        List<Integer> usefulList = jdbc.query(
                COUNT_REVIEW_USEFULNESS_QUERY,
                (rs, rowNum) -> rs.getInt("useful"),
                reviewId
        );
        return usefulList.isEmpty() ? 0 : usefulList.getFirst();
    }

    private void setReviewUsefulness(Review review) {
        review.setUseful(getReviewUsefulness(review.getReviewId()));
    }
}
