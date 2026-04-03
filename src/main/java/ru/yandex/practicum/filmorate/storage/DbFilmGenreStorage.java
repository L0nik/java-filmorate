package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmGenreRowMapper;

import java.util.Collection;

@Repository
public class DbFilmGenreStorage extends DbBaseStorage<FilmGenre> implements FilmGenreStorage {

    private static final String GET_ALL_QUERY = "SELECT * FROM film_genre";
    private static final String GET_GENRES_BY_FILM_ID_QUERY = "SELECT * FROM film_genre WHERE film_id = ?";

    public DbFilmGenreStorage(JdbcTemplate jdbc, FilmGenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<FilmGenre> getAll() {
        return findMany(GET_ALL_QUERY);
    }

    public Collection<FilmGenre> getGenresByFilmId(Long filmId) {
        return findMany(GET_GENRES_BY_FILM_ID_QUERY, filmId);
    }
}
