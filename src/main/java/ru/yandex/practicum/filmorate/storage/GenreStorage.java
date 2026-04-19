package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {

    Genre getGenreById(long id);

    Collection<Genre> getAllGenres();

    Collection<Genre> getGenresByFilmId(Long filmId);

    void checkIfGenreExists(long id);
}
