package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmLike;
import ru.yandex.practicum.filmorate.storage.mappers.FilmLikeRowMapper;

import java.util.Collection;

@Repository
public class DbFilmLikeStorage extends DbBaseStorage<FilmLike> implements FilmLikeStorage {
    private static final String GET_FILM_LIKES_QUERY = "SELECT * FROM film_likes WHERE film_id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM film_likes WHERE film_id = ?";
    private static final String PUT_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    public DbFilmLikeStorage(JdbcTemplate jdbc, FilmLikeRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<FilmLike> getAll() {
        return findMany(GET_ALL_QUERY);
    }

    public Collection<FilmLike> getLikesByFilmId(Long filmId) {
        return findMany(GET_FILM_LIKES_QUERY, filmId);
    }

    public void putLike(Long filmId, Long userId) {
        update(PUT_LIKE_QUERY, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        update(REMOVE_LIKE_QUERY, filmId, userId);
    }
}
