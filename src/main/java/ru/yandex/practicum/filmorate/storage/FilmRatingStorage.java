package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.Collection;

public interface FilmRatingStorage {

    FilmRating getRatingById(long id);

    Collection<FilmRating> getAllRatings();
}
