package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.ReviewReactionStorage;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewReactionService {
    private final ReviewReactionStorage reactionStorage;

    public void putLike(Long reviewId, Long userId) {
        reactionStorage.putReaction(reviewId, userId, true);
    }

    public void putDislike(Long reviewId, Long userId) {
        reactionStorage.putReaction(reviewId, userId, false);
    }

    public void deleteLike(Long reviewId, Long userId) {
        reactionStorage.deleteReaction(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        reactionStorage.deleteReaction(reviewId, userId);
    }
}
