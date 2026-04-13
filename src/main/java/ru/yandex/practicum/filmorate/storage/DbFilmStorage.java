package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository("dbFilmStorage")
@Slf4j
public class DbFilmStorage extends DbBaseStorage<Film> implements FilmStorage {

    private static final String GET_BY_ID_QUERY = "SELECT f.*, r.name AS rating_name FROM films AS f JOIN rating_mpa AS r ON f.rating_id = r.id WHERE f.id = ?";
    private static final String GET_ALL_QUERY = "SELECT f.*, r.name AS rating_name FROM films AS f JOIN rating_mpa AS r ON f.rating_id = r.id";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, rating_id)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private static final String GET_TOP_FILMS_BY_LIKES_QUERY = "SELECT films.*, r.name AS rating_name, top_films_ids.likes_count FROM films INNER JOIN" +
            " (SELECT f.id AS id, COUNT(fl.user_id) AS likes_count FROM films AS f JOIN film_likes AS fl ON f.id = fl.film_id GROUP BY f.id) AS top_films_ids" +
            " ON films.id = top_films_ids.id" +
            " JOIN rating_mpa AS r ON films.rating_id = r.id" +
            " ORDER BY top_films_ids.likes_count DESC LIMIT ?";

    public DbFilmStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film getFilmById(long id) {
        Optional<Film> filmOpt = findOne(GET_BY_ID_QUERY, id);
        if (filmOpt.isEmpty()) {
            String errorMessage = String.format("Фильм с id '%d' не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return filmOpt.get();
    }

    @Override
    public Film addFilm(Film newFilm) {
        log.info("Начало добавления фильма {}", newFilm);
        Long id = insert(
                INSERT_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId()
        );
        newFilm.setId(id);
        log.info("Добавлен новый фильм: {}", newFilm);
        return newFilm;
    }

    @Override
    public void updateFilm(Film newFilm) {
        log.info("Начало обновления фильма {}", newFilm);
        update(
                UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
        log.info("Обновлен фильм: {}", newFilm);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return findMany(GET_ALL_QUERY);
    }

    @Override
    public Collection<Film> getTopFilmsByLikes(int count) {
        return findMany(GET_TOP_FILMS_BY_LIKES_QUERY, count);
    }
}
