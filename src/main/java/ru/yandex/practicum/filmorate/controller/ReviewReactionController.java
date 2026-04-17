package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.ReviewReactionService;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewReactionController {

    private final ReviewReactionService reactionService;

    @PutMapping("/{reviewId}/like/{userId}")
    void putLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка отзыву {} от пользователя {}", reviewId, userId);
        reactionService.putLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    void deleteLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка отзыву {} от пользователя {}", reviewId, userId);
        reactionService.deleteLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    void putDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Получен запрос на добавление дизлайка отзыву {} от пользователя {}", reviewId, userId);
        reactionService.putDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    void deleteDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Получен запрос на удаления дизлайка отзыву {} от пользователя {}", reviewId, userId);
        reactionService.deleteDislike(reviewId, userId);
    }

}
