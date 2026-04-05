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
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DbGenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DbGenreStorage.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbGenreStorageTests {
    private final DbGenreStorage genreStorage;
    private final JdbcTemplate jdbc;

    @Test
    @DisplayName("Должен вернуться жанр, если он существует")
    public void testFindGenreByIdPositive() {
        Genre dbGenre = jdbc.queryForObject("SELECT * FROM genre WHERE id = ?",  new GenreRowMapper(), 1L);
        assertThat(dbGenre).isNotNull();
        Genre genre = genreStorage.getGenreById(1);

        assertThat(genre)
                .hasFieldOrPropertyWithValue("id", dbGenre.getId())
                .hasFieldOrPropertyWithValue("name", dbGenre.getName());
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException, если жанра нет")
    void testFindGenreByIdNegative() {
        long nonExistentId = 999L;

        assertThatThrownBy(() -> genreStorage.getGenreById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Жанр с id '" + nonExistentId + "' не найден");
    }

    @Test
    @DisplayName("Должен вернуть все жанры")
    void testGetAllGenres() {

        List<Genre> dbGenres = jdbc.query("SELECT * FROM genre", new GenreRowMapper());
        Collection<Genre> genres = genreStorage.getAllGenres();
        assertThat(genres).hasSize(dbGenres.size());

        dbGenres.forEach(dbGenre -> {
            Optional<Genre> genreOpt = genres.stream()
                    .filter(genre -> genre.getId().equals(dbGenre.getId()))
                    .findFirst();
            assertThat(genreOpt).isPresent();
            assertThat(genreOpt.get()).usingRecursiveComparison().isEqualTo(dbGenre);
        });
    }
}
