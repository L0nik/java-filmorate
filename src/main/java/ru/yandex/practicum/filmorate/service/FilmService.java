package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(
            FilmStorage filmStorage,
            @Qualifier("dbUserStorage") UserStorage userStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film newFilm) {
        validateFilm(newFilm);
        return filmStorage.addFilm(newFilm);
    }

    public Film updateFilm(Film newFilm) {

        if (newFilm.getId() == null) {
            String errorMessage = "Не указан id";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        Film film = filmStorage.getFilmById(newFilm.getId());

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

        filmStorage.updateFilm(film);

        log.info("Фильм успешно обновлен: {}", film);

        return film;
    }

    public void putLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        film.putLike(user.getId());
        log.info("Фильму {} добавлен лайк от пользователя {}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        film.removeLike(user.getId());
        log.info("Пользователь {} удалил лайк фильма {}", userId, filmId);
    }

    public Collection<Film> getTopFilmsByLikes(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .limit(count)
                .toList();
    }

    private void validateFilm(Film film) {
        log.info("Начало валидации фильма {}", film);
        film.validateName();
        film.validateDescription();
        film.validateReleaseDate();
        film.validateDuration();
        log.info("Валидация фильма завершилась успешно {}", film);
    }
}
