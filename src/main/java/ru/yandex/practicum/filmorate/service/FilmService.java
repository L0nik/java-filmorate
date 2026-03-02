package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

@Service
public class FilmService {

    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film newFilm) throws ValidationException {
        return filmStorage.addFilm(newFilm);
    }

    public Film updateFilm(Film newFilm) throws ValidationException, NotFoundException {
        return filmStorage.updateFilm(newFilm);
    }

    public void addLike(long filmId, long userId) throws NotFoundException {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        film.addLike(user.getId());
        log.info("Фильму {} добавлен лайк от пользователя {}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) throws NotFoundException {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        film.removeLike(user.getId());
        log.info("Пользователь {} удалил лайк фильма {}", userId, filmId);
    }

    public Collection<Film> getTopFilmsByLikes() {
        return getTopFilmsByLikes(10);
    }

    public Collection<Film> getTopFilmsByLikes(long count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(film -> film.getLikes().size()))
                .limit(count)
                .toList();
    }
}
