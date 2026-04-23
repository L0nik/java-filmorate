package ru.yandex.practicum.filmorate.storage;

import org.springframework.lang.Nullable;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film getFilmById(long id);

    Film addFilm(Film newFilm);

    void updateFilm(Film newFilm);

    Collection<Film> getAllFilms();

    Collection<Film> getTopFilmsByLikes(@Nullable Integer count, @Nullable Long genreId, @Nullable Integer year);

    Collection<Film> getFilmsByDirectorSortedByLikes(long directorId);

    Collection<Film> getFilmsByDirectorSortedByYear(long directorId);

    void checkIfFilmExists(long id);

    Collection<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector);

    void deleteFilm(long id);

    Collection<Film> getCommonFilms(long userId, long friendId);
}
