package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.Collection;

public interface FilmGenreStorage {
    Collection<FilmGenre> getAll();
    Collection<FilmGenre> getGenresByFilmId(Long filmId);
}
