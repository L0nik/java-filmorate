package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@RequestBody Review newReview) {
        log.info("Получен запрос на добавление отзыва: {}", newReview);
        return reviewService.addReview(newReview);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Получен запрос на получение отзыва по id: {}", id);
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteReviewById(@PathVariable Long id) {
        log.info("Получен запрос на удаление отзыва по id: {}", id);
        reviewService.deleteReviewById(id);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review newReview) {
        log.info("Получен запрос на обновление данных отзыва: {}", newReview);
        return reviewService.updateReview(newReview);
    }

}
