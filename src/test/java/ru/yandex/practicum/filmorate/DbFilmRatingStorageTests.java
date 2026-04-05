package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DbFilmRatingStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRatingRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DbFilmRatingStorage.class, FilmRatingRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbFilmRatingStorageTests {

    private final DbFilmRatingStorage ratingStorage;
    private final JdbcTemplate jdbc;

    @Test
    @DisplayName("Должен вернуться рейтинг, если он существует")
    public void testFindRatingByIdPositive() {
        FilmRating dbRating = jdbc.queryForObject("SELECT * FROM rating_mpa WHERE id = ?",  new FilmRatingRowMapper(), 1L);
        assertThat(dbRating).isNotNull();
        FilmRating rating = ratingStorage.getRatingById(1);

        assertThat(rating)
                .hasFieldOrPropertyWithValue("id", dbRating.getId())
                .hasFieldOrPropertyWithValue("name", dbRating.getName());
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException, если рейтинга нет")
    void testFindRatingByIdNegative() {
        long nonExistentId = 999L;

        assertThatThrownBy(() -> ratingStorage.getRatingById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Рейтинг с id '" + nonExistentId + "' не найден");
    }

    @Test
    @DisplayName("Должен вернуть все рейтинги")
    void testGetAllRatings() {
        List<Genre> dbRatings = jdbc.query("SELECT * FROM rating_mpa ORDER BY id", new GenreRowMapper());
        Collection<FilmRating> ratings = ratingStorage.getAllRatings();
        assertThat(ratings).hasSize(dbRatings.size());

        dbRatings.forEach(dbRating -> {
            Optional<FilmRating> ratingOpt = ratings.stream()
                    .filter(rating -> rating.getId().equals(dbRating.getId()))
                    .findFirst();
            assertThat(ratingOpt).isPresent();
            assertThat(ratingOpt.get()).usingRecursiveComparison().isEqualTo(dbRating);
        });
    }
}
