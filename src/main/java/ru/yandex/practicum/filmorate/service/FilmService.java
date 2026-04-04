package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
        Collection<Genre> genres = genreStorage.getAllGenres();
        Collection<Genre> filmGenres = filmGenreStorage.getGenresByFilmId(id).stream()
                .map(FilmGenre::getGenreId)
                .sorted(Long::compare)
                .flatMap(
                        genreId -> genres.stream()
                                .filter(genre -> genreId.equals(genre.getId()))
                                .findFirst()
                                .stream()
                )
                .toList();
        film.addLikes(likes);
        film.addGenres(filmGenres);
        return film;
    }

    public Collection<Film> getAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();
        Collection<FilmLike> likes = likeStorage.getAll();
        Collection<Genre> genres = genreStorage.getAllGenres();
        Collection<FilmGenre> filmGenres = filmGenreStorage.getAll();
        films.forEach(film -> {
            film.addLikes(
                    likes.stream()
                            .filter(like -> film.getId().equals(like.getFilmId()))
                            .map(FilmLike::getUserId)
                            .toList()
            );
            film.addGenres(
                    filmGenres.stream()
                            .filter(filmGenre -> film.getId().equals(filmGenre.getFilmId()))
                            .map(FilmGenre::getGenreId)
                            .sorted(Long::compare)
                            .flatMap(
                                    genreId -> genres.stream()
                                            .filter(genre -> genre.getId().equals(genreId))
                                            .findFirst()
                                            .stream()
                            )
                            .toList()
            );
        });
        return films;
    }

    public Film addFilm(Film newFilm) {
        validateFilm(newFilm);
        if (newFilm.getMpa() != null) {
            newFilm.setMpa(ratingStorage.getRatingById(newFilm.getMpa().getId()));
        }
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            newFilm.getGenres().forEach(
                    genre -> {
                        Genre dbGenre = genreStorage.getGenreById(genre.getId());
                        genre.setName(dbGenre.getName());
                    }
            );
        }
        newFilm = filmStorage.addFilm(newFilm);
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            filmGenreStorage.addGenresToFilm(
                    newFilm.getId(),
                    newFilm.getGenres().stream().map(Genre::getId).toList()
            );
        }
        return newFilm;
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
            film.validateName();
            film.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            film.validateDescription();
            film.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            film.validateReleaseDate();
            film.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            film.validateDuration();
            film.setDuration(newFilm.getDuration());
        }

        if (newFilm.getMpa() != null) {
            film.setMpa(ratingStorage.getRatingById(newFilm.getMpa().getId()));
        }

        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            filmGenreStorage.updateGenresOfFilm(
                    newFilm.getId(),
                    newFilm.getGenres().stream().map(Genre::getId).toList()
            );
        }

        filmStorage.updateFilm(film);

        log.info("Фильм успешно обновлен: {}", film);

        return film;
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
