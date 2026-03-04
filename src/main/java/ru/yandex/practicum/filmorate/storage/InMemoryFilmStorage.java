package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film getFilmById(long id) {
        Film film = films.get(id);
        if (film == null) {
            String errorMessage = String.format("Фильм с id '%d' не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film addFilm(Film newFilm) {
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен новый фильм: {}", newFilm);
        return newFilm;
    }

    @Override
    public void updateFilm(Film newFilm) {
        films.put(newFilm.getId(), newFilm);
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
