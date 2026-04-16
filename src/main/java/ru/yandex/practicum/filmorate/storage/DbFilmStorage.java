package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository("dbFilmStorage")
@Slf4j
public class DbFilmStorage extends DbBaseStorage<Film> implements FilmStorage {

    private final DirectorRowMapper directorRowMapper;

    private static final String GET_BY_ID_QUERY = "SELECT f.*, r.name AS rating_name, d.id AS director_id, d.name AS director_name FROM films AS f " +
            "JOIN rating_mpa AS r ON f.rating_id = r.id " +
            "LEFT JOIN film_directors AS fd ON f.id = fd.film_id " +
            "LEFT JOIN directors AS d ON fd.director_id = d.id " +
            "WHERE f.id = ?";
    private static final String GET_ALL_QUERY = "SELECT f.*, r.name AS rating_name, d.id AS director_id, d.name AS director_name FROM films AS f JOIN rating_mpa AS r ON f.rating_id = r.id " +
            "LEFT JOIN film_directors AS fd ON f.id = fd.film_id " +
            "LEFT JOIN directors AS d ON fd.director_id = d.id";
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, rating_id)" +
            " VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
    private static final String GET_TOP_FILMS_BY_LIKES_QUERY = "SELECT films.*, r.name AS rating_name, top_films_ids.likes_count FROM films INNER JOIN" +
            " (SELECT f.id AS id, COUNT(fl.user_id) AS likes_count FROM films AS f JOIN film_likes AS fl ON f.id = fl.film_id GROUP BY f.id) AS top_films_ids" +
            " ON films.id = top_films_ids.id" +
            " JOIN rating_mpa AS r ON films.rating_id = r.id" +
            " ORDER BY top_films_ids.likes_count DESC LIMIT ?";
    private static final String GET_FILMS_BY_DIRECTOR_SORTED_BY_LIKES =
            "SELECT f.*, r.name AS rating_name, d.id AS director_id, d.name AS director_name FROM films AS f " +
                    "JOIN rating_mpa AS r ON f.rating_id = r.id " +
                    "JOIN film_directors AS fd ON f.id = fd.film_id " +
                    "JOIN directors AS d ON fd.director_id = d.id " +
                    "WHERE d.id = ? ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) DESC";
    private static final String GET_FILMS_BY_DIRECTOR_SORTED_BY_YEAR = "SELECT f.*, r.name AS rating_name FROM films AS f " +
            "JOIN rating_mpa AS r ON f.rating_id = r.id " +
            "JOIN film_directors AS fd ON f.id = fd.film_id " +
            "WHERE fd.director_id = ? ORDER BY f.release_date";
    private static final String INSERT_FILM_DIRECTOR_QUERY = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String LOAD_DIRECTORS_QUERY = "SELECT d.id, d.name FROM directors AS d " +
            "JOIN film_directors AS fd ON d.id = fd.director_id WHERE fd.film_id = ?";

    public DbFilmStorage(JdbcTemplate jdbc, FilmRowMapper mapper, DirectorRowMapper directorRowMapper) {
        super(jdbc, mapper);
        this.directorRowMapper = directorRowMapper;
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

        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            jdbc.batchUpdate(
                    INSERT_FILM_DIRECTOR_QUERY,
                    newFilm.getDirectors(),
                    newFilm.getDirectors().size(),
                    (ps, director) -> {
                        ps.setLong(1, newFilm.getId());
                        ps.setLong(2, director.getId());
                    }
            );
        }
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
        delete(DELETE_FILM_DIRECTORS_QUERY, newFilm.getId());

        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            jdbc.batchUpdate(
                    INSERT_FILM_DIRECTOR_QUERY,
                    newFilm.getDirectors(),
                    newFilm.getDirectors().size(),
                    (ps, directorDto) -> {
                        ps.setLong(1, newFilm.getId());
                        ps.setLong(2, directorDto.getId());
                    }
            );
        }
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

    @Override
    public Collection<Film> getFilmsByDirectorSortedByYear(long directorId) {
        Collection<Film> films = findMany(GET_FILMS_BY_DIRECTOR_SORTED_BY_YEAR, directorId);

        films.forEach(film -> {
            film.setDirectors(loadDirectors(film.getId()));
        });

        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirectorSortedByLikes(long directorId) {
        Collection<Film> films = findMany(GET_FILMS_BY_DIRECTOR_SORTED_BY_LIKES, directorId);

        films.forEach(film -> {
            film.setDirectors(loadDirectors(film.getId()));
        });

        return films;
    }

    protected void delete(String query, Object... params) {
        jdbc.update(query, params);
    }

    private List<Director> loadDirectors(long filmId) {
        return jdbc.query(
                LOAD_DIRECTORS_QUERY,
                directorRowMapper,
                filmId
        );
    }
}
