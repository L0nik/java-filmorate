package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review newReview) {
        log.info("Получен запрос на добавление отзыва: {}", newReview);
        return reviewService.addReview(newReview);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Получен запрос на получение отзыва по id: {}", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public Collection<Review> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(required = false) Integer count
    ) {
        log.info("Получен запрос на получение списка отзывов (filmId = {}, count = {})", filmId, count);
        if (count == null) {
            count = 10;
        }
        return reviewService.getReviews(filmId, count);
    }

    @DeleteMapping("/{id}")
    public void deleteReviewById(@PathVariable Long id) {
        log.info("Получен запрос на удаление отзыва по id: {}", id);
        reviewService.deleteReviewById(id);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review newReview) {
        log.info("Получен запрос на обновление данных отзыва: {}", newReview);
        return reviewService.updateReview(newReview);
    }

}
