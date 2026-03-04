package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public Film getFilmById(long id);

    public Film addFilm(Film newFilm);

    public Film updateFilm(Film newFilm);

    public Collection<Film> getAllFilms();
}
