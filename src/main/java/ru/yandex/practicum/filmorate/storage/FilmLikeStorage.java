package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmLike;

import java.util.Collection;

public interface FilmLikeStorage {
    Collection<FilmLike> getAll();
    Collection<FilmLike> getLikesByFilmId(Long filmId);
    void putLike(Long filmId, Long userId);
    void removeLike(Long filmId, Long userId);
}
