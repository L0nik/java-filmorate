package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmGenreRowMapper implements RowMapper<FilmGenre> {
    @Override
    public FilmGenre mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmGenre genre = new FilmGenre();
        genre.setId(rs.getLong("id"));
        genre.setFilmId(rs.getLong("film_id"));
        genre.setGenreId(rs.getLong("genre_id"));
        return genre;
    }
}
