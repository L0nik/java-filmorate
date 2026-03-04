package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film getFilmById(long id);

    Film addFilm(Film newFilm);

    void updateFilm(Film newFilm);

    Collection<Film> getAllFilms();
}
