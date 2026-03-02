package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.BaseModel;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Map;

public interface FilmStorage {

    public Film addFilm(Film newFilm);
    public Film updateFilm(Film newFilm);
    public Collection<Film> getAllFilms();
}
