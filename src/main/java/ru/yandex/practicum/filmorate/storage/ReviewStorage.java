package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    Review getReviewById(long id);

    Review addReview(Review newReview);

    void updateReview(Review newReview);

    void deleteReviewById(long id);

}
