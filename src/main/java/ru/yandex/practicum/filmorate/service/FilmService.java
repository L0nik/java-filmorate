package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmLike;
import ru.yandex.practicum.filmorate.model.FilmSearchField;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final FilmRatingStorage ratingStorage;
    private final FilmLikeStorage likeStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final DirectorStorage directorStorage;
    private final EventStorage eventStorage;

    public FilmService(
            @Qualifier("dbFilmStorage") FilmStorage filmStorage,
            @Qualifier("dbUserStorage") UserStorage userStorage,
            GenreStorage genreStorage,
            FilmRatingStorage ratingStorage,
            FilmLikeStorage likeStorage,
            FilmGenreStorage filmGenreStorage,
            DirectorStorage directorStorage,
            EventStorage eventStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingStorage;
        this.likeStorage = likeStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.directorStorage = directorStorage;
        this.eventStorage = eventStorage;
    }

    public Film getFilmById(long id) {
        Film film = filmStorage.getFilmById(id);
        Collection<Long> likes = likeStorage.getLikesByFilmId(id).stream()
                .map(FilmLike::getUserId)
                .toList();
        film.addLikes(likes);
        film.addGenres(genreStorage.getGenresByFilmId(id));
        film.setDirectors(directorStorage.getDirectorsByFilmId(id));
        return film;
    }

    public Collection<Film> getAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();
        films.forEach(film -> {
                    film.addGenres(genreStorage.getGenresByFilmId(film.getId()));
                    film.setDirectors(directorStorage.getDirectorsByFilmId(film.getId()));
                });
        return films;
    }

    public Film addFilm(Film newFilm) {
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
        return getFilmById(newFilm.getId());
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
            film.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            film.setDescription(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            film.setReleaseDate(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            film.setDuration(newFilm.getDuration());
        }

        if (newFilm.getMpa() != null) {
            film.setMpa(ratingStorage.getRatingById(newFilm.getMpa().getId()));
        }

        if (newFilm.getGenres() != null) {
            filmGenreStorage.updateGenresOfFilm(
                    newFilm.getId(),
                    newFilm.getGenres().stream().map(Genre::getId).toList()
            );
        }

        if (newFilm.getDirectors() != null) {
            film.setDirectors(newFilm.getDirectors());
        }

        filmStorage.updateFilm(film);

        log.info("Фильм успешно обновлен: {}", film);

        return getFilmById(film.getId());
    }

    public void putLike(long filmId, long userId) {
        filmStorage.checkIfFilmExists(filmId);
        userStorage.checkIfUserExists(userId);
        likeStorage.putLike(filmId, userId);
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
        log.info("Фильму {} добавлен лайк от пользователя {}", filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        filmStorage.checkIfFilmExists(filmId);
        userStorage.checkIfUserExists(userId);
        likeStorage.removeLike(filmId, userId);
        eventStorage.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
        log.info("Пользователь {} удалил лайк фильма {}", userId, filmId);
    }

    public void deleteFilm(long id) {
        filmStorage.deleteFilm(id);
        log.info("Фильм {} удален", id);
    }

    public Collection<Film> getTopFilmsByLikes(@Nullable Integer count, @Nullable Long genreId, @Nullable Integer year) {
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
        if (genreId != null) {
            genreStorage.checkIfGenreExists(genreId);
        }
        Collection<Film> films = filmStorage.getTopFilmsByLikes(count, genreId, year);
        films.forEach(film -> {
            film.addGenres(genreStorage.getGenresByFilmId(film.getId()));
            film.setDirectors(directorStorage.getDirectorsByFilmId(film.getId()));
        });
        return films;
    }

    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        directorStorage.checkIfDirectorExists(directorId);
        Collection<Film> films;
        if (sortBy.equals("likes")) {
            films = filmStorage.getFilmsByDirectorSortedByLikes(directorId);
        } else if (sortBy.equals("year")) {
            films = filmStorage.getFilmsByDirectorSortedByYear(directorId);
        } else {
            throw new ValidationException("sortBy должно быть 'likes' or 'year'");
        }

        for (Film film : films) {
            film.addGenres(genreStorage.getGenresByFilmId(film.getId()));
            film.setDirectors(directorStorage.getDirectorsByFilmId(film.getId()));
        }

        return films;
    }

    public Collection<Film> searchFilms(String query, Set<FilmSearchField> fields) {

        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }

        boolean searchByTitle = fields.contains(FilmSearchField.TITLE);
        boolean searchByDirector = fields.contains(FilmSearchField.DIRECTOR);

        if (!searchByTitle && !searchByDirector) {
            return Collections.emptyList();
        }

        Collection<Film> films = filmStorage.searchFilms(query, searchByTitle, searchByDirector);

        for (Film film : films) {
            film.getGenres().addAll(
                    genreStorage.getGenresByFilmId(film.getId())
            );

            film.getDirectors().addAll(
                    directorStorage.getDirectorsByFilmId(film.getId())
            );
        }

        return films;
    }

    public Collection<Film> getCommonFilms(long userId, long friendId) {

        if (userId <= 0) {
            throw new ValidationException("Некорректный id пользователя: " + userId);
        }
        if (friendId <= 0) {
            throw new ValidationException("Некорректный id пользователя: " + friendId);
        }

        userStorage.checkIfUserExists(userId);
        userStorage.checkIfUserExists(friendId);

        Collection<Film> commonFilms = filmStorage.getCommonFilms(userId, friendId);

        commonFilms.forEach(film -> {
            film.addGenres(genreStorage.getGenresByFilmId(film.getId()));
            film.setDirectors(directorStorage.getDirectorsByFilmId(film.getId()));
        });

        return commonFilms;
    }
}
