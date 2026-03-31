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

    private final static String GET_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private final static String GET_ALL_QUERY = "SELECT * FROM films";
    private final static String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, rating_id)" +
            " VALUES (?, ?, ?, ?, ?)";
    private final static String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";

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
                newFilm.getRatingId()
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
                newFilm.getRatingId(),
                newFilm.getId()
        );
        log.info("Обновлен фильм: {}", newFilm);
    }

    @Override
    public Collection<Film> getAllFilms() {
        return findMany(GET_ALL_QUERY);
    }
}
