package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film addFilm(@RequestBody Film newFilm) {
        log.info("Получен запрос на добавление фильма: {}", newFilm);
        return filmService.addFilm(newFilm);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма: {}", newFilm);
        return filmService.updateFilm(newFilm);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable long id) {
        log.info("Получен запрос на получение фильма по id {}", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(
            @PathVariable long id,
            @PathVariable long userId
    ) {
        log.info("Получен запрос на добавление лайка фильму {} пользователем {}", id, userId);
        filmService.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable long id,
            @PathVariable long userId
    ) {
        log.info("Получен запрос на удаление лайка фильму {} пользователем {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getMostPopularFilms(
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year
    ) {
        log.info(
                "Получен запрос на получение самых популярных фильмов (count = {}, genreId = {}, year = {})",
                count,
                genreId,
                year
        );
        if (count != null && count < 0) {
            throw new ValidationException("Параметр count не может быть меньше 0");
        }
        if (genreId != null && genreId <= 0) {
            throw new ValidationException("Параметр genreId должен быть положительным числом");
        }
        if (year != null && year <= 0) {
            throw new ValidationException("Год должен быть положительным числом");
        }
        if (count == null && genreId == null && year == null) {
            count = 10;
        }
        return filmService.getTopFilmsByLikes(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam String sortBy
    ) {
        log.info("Получен запрос на получение фильмов режиссёра {} с сортировкой {}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(
            @RequestParam String query,
            @RequestParam String by
    ) {
        log.info("Получен запрос на поиск фильмов query={}, by={}", query, by);
        return filmService.searchFilms(query, by);
    }
}
