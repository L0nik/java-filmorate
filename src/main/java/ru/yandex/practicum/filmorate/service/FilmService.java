package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmLike;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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

    public FilmService(
            @Qualifier("dbFilmStorage") FilmStorage filmStorage,
            @Qualifier("dbUserStorage") UserStorage userStorage,
            GenreStorage genreStorage,
            FilmRatingStorage ratingStorage,
            FilmLikeStorage likeStorage,
            FilmGenreStorage filmGenreStorage,
            DirectorStorage directorStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingStorage;
        this.likeStorage = likeStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.directorStorage = directorStorage;
    }

    public Film getFilmById(long id) {
        Film film = filmStorage.getFilmById(id);
        Collection<Long> likes = likeStorage.getLikesByFilmId(id).stream()
                .map(FilmLike::getUserId)
                .toList();
        film.addLikes(likes);
        film.addGenres(genreStorage.getGenresByFilmId(id));
        return film;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
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

        if (newFilm.getMpa() != null) {
            film.setMpa(ratingStorage.getRatingById(newFilm.getMpa().getId()));
        }

        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
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

    public void deleteFilm(long id) {
        filmStorage.deleteFilm(id);
        log.info("Фильм {} удален", id);
    }

    public Collection<Film> getTopFilmsByLikes(int count) {
        return filmStorage.getTopFilmsByLikes(count);
        /*return getAllFilms().stream()
                .sorted((film1, film2) -> film2.getLikes().size() - film1.getLikes().size())
                .limit(count)
                .toList();*/
    }

    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        if (sortBy.equals("likes")) {
            return filmStorage.getFilmsByDirectorSortedByLikes(directorId);
        }
        if (sortBy.equals("year")) {
            return filmStorage.getFilmsByDirectorSortedByYear(directorId);
        }
        throw new ValidationException("sortBy must be 'likes' or 'year'");
    }

    public Collection<Film> searchFilms(String query, String by) {

        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        if (by == null || by.isBlank()) {
            return Collections.emptyList();
        }

        String[] fields = by.toLowerCase().split(",");
        Set<String> searchFields = new HashSet<>();

        for (String field : fields) {
            searchFields.add(field.trim());
        }

        boolean searchByTitle = searchFields.contains("title");
        boolean searchByDirector = searchFields.contains("director");

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

    private void validateFilm(Film film) {
        log.info("Начало валидации фильма {}", film);
        film.validateName();
        film.validateDescription();
        film.validateReleaseDate();
        film.validateDuration();
        log.info("Валидация фильма завершилась успешно {}", film);
    }
}
