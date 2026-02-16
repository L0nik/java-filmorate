package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    static Consumer<Optional<String>> validationErrorConsumer = (errorOpt) -> {
        if (errorOpt.isPresent())
            throw new ValidationException(errorOpt.get());
    };

    @PostMapping
    public Film addFilm(@RequestBody Film newFilm) throws ValidationException {
        validateFilm(newFilm);
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) throws ValidationException, NotFoundException {

        if (newFilm.getId() == null) {
            throw new ValidationException("Не указан id");
        }

        Film film = films.get(newFilm.getId());
        if (film == null) {
            throw new NotFoundException(String.format("Фильм с id '%d' не найден", newFilm.getId()));
        }

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

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
