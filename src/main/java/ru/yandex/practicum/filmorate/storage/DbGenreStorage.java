package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@Slf4j
public class DbGenreStorage extends DbBaseStorage<Genre> implements GenreStorage {

    private static final String GET_BY_ID_QUERY = "SELECT * FROM genre WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM genre";
    private static final String GET_GENRES_BY_FILM_ID_QUERY = "SELECT g.id AS id, g.name AS name" +
            " FROM film_genre AS fg JOIN genre AS g" +
            " ON fg.genre_id = g.id AND film_id = ?" +
            " ORDER BY id ASC";

    public DbGenreStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Genre getGenreById(long id) {
        Optional<Genre> genreOpt = findOne(GET_BY_ID_QUERY, id);
        if (genreOpt.isPresent()) {
            return genreOpt.get();
        } else {
            String errorMessage = String.format("Жанр с id '%d' не найден", id);
            log.info(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    public Collection<Genre> getAllGenres() {
        return findMany(GET_ALL_QUERY);
    }

    public Collection<Genre> getGenresByFilmId(Long filmId) {
        return findMany(GET_GENRES_BY_FILM_ID_QUERY, filmId);
    }
}
