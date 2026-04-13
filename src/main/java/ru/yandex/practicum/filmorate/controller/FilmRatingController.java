package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.service.FilmRatingService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class FilmRatingController {
    private final FilmRatingService ratingService;

    @GetMapping
    public Collection<FilmRating> getAllRatings() {
        log.info("Получен запрос на получение всех рейтингов фильмов");
        return ratingService.getAllRatings();
    }

    @GetMapping("/{id}")
    public FilmRating getRatingById(@PathVariable Long id) {
        log.info("Получен запрос на получение рейтинга фильма по id {}", id);
        return ratingService.getRatingById(id);
    }
}
