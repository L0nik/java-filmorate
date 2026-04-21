package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewReactionStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final ReviewReactionStorage reactionStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventStorage eventStorage;

    public Review getReviewById(long id) {
        return reviewStorage.getReviewById(id);
    }

    public Collection<Review> getReviews(Long filmId, Integer count) {
        Collection<Review> reviews = null;
        if (filmId == null) {
            reviews = reviewStorage.getReviews(count);
        } else {
            reviews = reviewStorage.getReviewsByFilmId(filmId, count);
        }
        return reviews.stream()
                .sorted((review1, review2) -> review1.getUseful() - review2.getUseful())
                .toList();
    }

    public Review addReview(Review newReview) {
        validateReview(newReview);
        Review review = reviewStorage.addReview(newReview);
        eventStorage.addEvent(review.getUserId(), "REVIEW", "ADD", review.getReviewId());
        return review;
    }

    public Review updateReview(Review newReview) {
        if (newReview.getReviewId() == null) {
            String errorMessage = "Не указан id";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        Review review = reviewStorage.getReviewById(newReview.getReviewId());

        log.info("Начало обновления отзыва: {}", review);

        if (newReview.getContent() != null) {
            newReview.validateContent();
            review.setContent(newReview.getContent());
        }

        if (newReview.getUserId() != null) {
            newReview.validateUserId();
            userStorage.checkIfUserExists(newReview.getUserId());
            review.setUserId(newReview.getUserId());
        }

        if (newReview.getFilmId() != null) {
            newReview.validateFilmId();
            filmStorage.checkIfFilmExists(newReview.getFilmId());
            review.setFilmId(newReview.getFilmId());
        }

        if (newReview.getIsPositive() != null) {
            newReview.validateIsPositive();
            review.setIsPositive(newReview.getIsPositive());
        }

        reviewStorage.updateReview(review);
        eventStorage.addEvent(review.getUserId(), "REVIEW", "UPDATE", review.getReviewId());

        log.info("Отзыв успешно обновлен: {}", review);

        return review;
    }

    public void deleteReviewById(long id) {
        Review review = reviewStorage.getReviewById(id);
        reviewStorage.deleteReviewById(id);
        eventStorage.addEvent(review.getUserId(), "REVIEW", "REMOVE", review.getReviewId());
    }

    private void validateReview(Review review) {
        log.info("Начало валидации отзыва {}", review);
        review.validateContent();
        review.validateUserId();
        review.validateFilmId();
        review.validateIsPositive();
        userStorage.checkIfUserExists(review.getUserId());
        filmStorage.checkIfFilmExists(review.getFilmId());
        log.info("Валидация отзыва завершилась успешно {}", review);
    }
}
