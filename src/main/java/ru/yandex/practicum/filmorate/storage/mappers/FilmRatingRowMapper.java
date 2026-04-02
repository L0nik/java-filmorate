package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRatingRowMapper implements RowMapper<FilmRating> {
    @Override
    public FilmRating mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmRating rating = new FilmRating();
        rating.setId(rs.getLong("id"));
        rating.setName(rs.getString("name"));
        return rating;
    }
}
