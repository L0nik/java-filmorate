package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Collection;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final FilmRatingStorage ratingStorage;
    private final FilmLikeStorage likeStorage;
    private final FilmGenreStorage filmGenreStorage;

    public FilmService(
            @Qualifier("dbFilmStorage") FilmStorage filmStorage,
            @Qualifier("dbUserStorage") UserStorage userStorage,
            GenreStorage genreStorage,
            FilmRatingStorage ratingStorage,
            FilmLikeStorage likeStorage,
            FilmGenreStorage filmGenreStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingStorage;
        this.likeStorage = likeStorage;
        this.filmGenreStorage = filmGenreStorage;
    }

    public Film getFilmById(long id) {
        Film film = filmStorage.getFilmById(id);
        Collection<Long> likes = likeStorage.getLikesByFilmId(id).stream()
                .map(FilmLike::getUserId)
                .toList();
        Collection<Long> genres = filmGenreStorage.getGenresByFilmId(id).stream()
                .map(FilmGenre::getGenreId)
                .toList();
        film.addLikes(likes);
        film.addGenres(genres);
        return film;
    }

    public Collection<Film> getAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();
        Collection<FilmLike> likes = likeStorage.getAll();
        Collection<FilmGenre> genres = filmGenreStorage.getAll();
        films.forEach(film -> {
            film.addLikes(
                    likes.stream()
                            .filter(like -> film.getId().equals(like.getFilmId()))
                            .map(FilmLike::getUserId)
                            .toList()
            );
            film.addGenres(
                    genres.stream()
                            .filter(filmGenre -> film.getId().equals(filmGenre.getFilmId()))
                            .map(FilmGenre::getGenreId)
                            .toList()
            );
        });
        return films;
    }

    public FilmDto addFilm(NewFilmRequest request) {
        Film newFilm = FilmMapper.mapToFilm(request);
        validateFilm(newFilm);
        newFilm = filmStorage.addFilm(newFilm);
        return FilmMapper.mapToFilmDto(newFilm);
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {

        if (request.getId() == null) {
            String errorMessage = "Не указан id";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        Film film = filmStorage.getFilmById(request.getId());

        log.info("Начало обновления фильма: {}", film);
        if (request.getName() != null) {
            film.setName(request.getName());
            film.validateName();
        }

        if (request.getDescription() != null) {
            film.setDescription(request.getDescription());
            film.validateDescription();
        }

        if (request.getReleaseDate() != null) {
            film.setReleaseDate(request.getReleaseDate());
            film.validateReleaseDate();
        }

        if (request.getDuration() != null) {
            film.setDuration(request.getDuration());
            film.validateDuration();
        }

        if (request.getMpa() != null) {
            film.setRatingId(request.getMpa().getId());
        }

        filmStorage.updateFilm(film);

        log.info("Фильм успешно обновлен: {}", film);

        return FilmMapper.mapToFilmDto(film);
    }

    public void putLike(long filmId, long userId) {
        likeStorage.putLike(filmId, userId);
        log.info("Фильму {} добавлен лайк от пользователя {}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        likeStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк фильма {}", userId, filmId);
    }

    public Collection<Film> getTopFilmsByLikes(int count) {
        return getAllFilms().stream()
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .limit(count)
                .toList();
    }

    public Collection<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(long id) {
        return genreStorage.getGenreById(id);
    }

    public Collection<FilmRating> getAllRatings() {
        return ratingStorage.getAllRatings();
    }

    public FilmRating getRatingById(long id) {
        return ratingStorage.getRatingById(id);
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
