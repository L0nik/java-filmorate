package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmStorage filmStorage;

    public FilmController(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @PostMapping
    public Film addFilm(@RequestBody Film newFilm) throws ValidationException {
        log.info("Получен запрос на добавление фильма: {}", newFilm);
        return filmStorage.addFilm(newFilm);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) throws ValidationException, NotFoundException {

        log.info("Получен запрос на обновление фильма: {}", newFilm);
        return filmStorage.updateFilm(newFilm);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

}
