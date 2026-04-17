package ru.yandex.practicum.filmorate.storage;

public interface ReviewReactionStorage {

    void putReaction(long reviewId, long userId, boolean isUseful);

    void deleteReaction(long reviewId, long userId);

}
