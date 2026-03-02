package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film addFilm(Film newFilm) throws ValidationException {
        validateFilm(newFilm);
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен новый фильм: {}", newFilm);
        return newFilm;
    }

    @Override
    public Film updateFilm(Film newFilm) throws ValidationException, NotFoundException {

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
            newFilm.validateName();
            film.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            newFilm.validateDescription();
            film.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            newFilm.validateReleaseDate();
            film.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            newFilm.validateDuration();
            film.setDuration(newFilm.getDuration());
        }
        log.info("Фильм успешно обновлен: {}", film);

        return film;
    }

    private void validateFilm(Film film) throws ValidationException {
        film.validateName();
        film.validateDescription();
        film.validateReleaseDate();
        film.validateDuration();
    }

    protected long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
