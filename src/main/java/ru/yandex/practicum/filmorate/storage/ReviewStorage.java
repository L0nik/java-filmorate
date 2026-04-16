package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    Review getReviewById(long id);

    Review addReview(Review newReview);

    void updateReview(Review newReview);

    void deleteReviewById(long id);

    Collection<Review> getReviews(Integer count);

    Collection<Review> getReviewsByFilmId(Long filmId, Integer count);

    void checkIfReviewExists(long id);

}
