package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film newFilm) throws ValidationException {
        log.info("Получен запрос на добавление фильма: {}", newFilm);
        validateFilm(newFilm);
        newFilm.setId(getNextId(films));
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен новый фильм: {}", newFilm);
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) throws ValidationException, NotFoundException {

        log.info("Получен запрос на обновление фильма: {}", newFilm);

        if (newFilm.getId() == null) {
            String errorMessage = "Не указан id";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        Film film = films.get(newFilm.getId());
        if (film == null) {
            String errorMessage = String.format("Фильм с id '%d' не найден", newFilm.getId());
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        log.info("Начало обновления фильма: {}", film);
        if (newFilm.getName() != null) {
            validationErrorConsumer.accept(newFilm.validateName());
            film.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            validationErrorConsumer.accept(newFilm.validateDescription());
            film.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            validationErrorConsumer.accept(newFilm.validateReleaseDate());
            film.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            validationErrorConsumer.accept(newFilm.validateDuration());
            film.setDuration(newFilm.getDuration());
        }
        log.info("Фильм успешно обновлен: {}", film);

        return film;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    private void validateFilm(Film film) throws ValidationException {
        validationErrorConsumer.accept(film.validateName());
        validationErrorConsumer.accept(film.validateDescription());
        validationErrorConsumer.accept(film.validateReleaseDate());
        validationErrorConsumer.accept(film.validateDuration());
    }

}
