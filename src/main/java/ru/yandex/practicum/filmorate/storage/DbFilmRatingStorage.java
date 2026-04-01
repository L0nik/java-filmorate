package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRatingRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@Slf4j
public class DbFilmRatingStorage extends DbBaseStorage<FilmRating> {

    private final static String GET_BY_ID_QUERY = "SELECT * FROM rating_mpa WHERE id = ?";
    private final static String GET_ALL_QUERY = "SELECT * FROM rating_mpa";

    public DbFilmRatingStorage(JdbcTemplate jdbc, FilmRatingRowMapper mapper) {
        super(jdbc, mapper);
    }

    public FilmRating getRatingById(long id) {
        Optional<FilmRating> ratingOpt = findOne(GET_BY_ID_QUERY, id);
        if (ratingOpt.isPresent()) {
            return ratingOpt.get();
        } else {
            String errorMessage = String.format("Рейтинг с id '%d' не найден", id);
            log.info(errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    public Collection<FilmRating> getAllRatings() {
        return findMany(GET_ALL_QUERY);
    }
}
