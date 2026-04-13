package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.DbFilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DbFilmStorage.class, FilmRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbFilmStorageTests {
    private final DbFilmStorage filmStorage;
    private final JdbcTemplate jdbc;

    private static final String GET_FILM_BY_ID_QUERY = "SELECT f.*, r.name AS rating_name " +
            "FROM films AS f JOIN rating_mpa AS r ON f.rating_id = r.id WHERE f.id = ?";
    private static final String GET_ALL_FILMS_QUERY = "SELECT f.*, r.name AS rating_name " +
            "FROM films AS f JOIN rating_mpa AS r ON f.rating_id = r.id";

    @BeforeEach
    public void beforeEach() {
        jdbc.update("DELETE FROM films");
        jdbc.update("INSERT INTO films (id, name, description, release_date, duration, rating_id)" +
                " VALUES (1, 'test name', 'test description', '2000-01-01', 123, 1)");
        jdbc.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 2");
    }

    @Test
    @DisplayName("Должен вернуться фильм, если он существует")
    public void testFindFilmByIdPositive() {
        Film film = filmStorage.getFilmById(1);

        assertThat(film)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "test name");
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException, если фильма нет")
    void testFindFilmByIdNegative() {
        long nonExistentId = 999L;

        assertThatThrownBy(() -> filmStorage.getFilmById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с id '" + nonExistentId + "' не найден");
    }

    @Test
    @DisplayName("Должен успешно сохранить фильм и вернуть его")
    void testAddFilm() {

        FilmRating mpa = new FilmRating();
        mpa.setId(2L);
        mpa.setName("PG");

        Film newFilm = new Film();
        newFilm.setName("test name new");
        newFilm.setDescription("test description new");
        newFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        newFilm.setDuration(100);
        newFilm.setMpa(mpa);

        Film savedFilm = filmStorage.addFilm(newFilm);

        assertThat(savedFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "test name new")
                .hasFieldOrProperty("id");

        assertThat(savedFilm.getId()).isPositive();

        Film filmFromDb = jdbc.queryForObject(GET_FILM_BY_ID_QUERY, new FilmRowMapper(), savedFilm.getId());

        assertThat(filmFromDb)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedFilm);
    }

    @Test
    @DisplayName("Должен успешно обновить фильм")
    void testUpdateFilm() {

        long filmId = 1L;

        Film filmBefore = jdbc.queryForObject(GET_FILM_BY_ID_QUERY, new FilmRowMapper(), filmId);
        assertThat(filmBefore).isNotNull();

        FilmRating mpa = new FilmRating();
        mpa.setId(3L);
        mpa.setName("PG-13");

        Film updatedFilm = new Film();
        updatedFilm.setId(filmId);
        updatedFilm.setName(filmBefore.getName());
        updatedFilm.setDescription(filmBefore.getDescription() + " updated");
        updatedFilm.setReleaseDate(filmBefore.getReleaseDate());
        updatedFilm.setDuration(filmBefore.getDuration() + 10);
        updatedFilm.setMpa(mpa);

        filmStorage.updateFilm(updatedFilm);

        Film dbFilm = jdbc.queryForObject(GET_FILM_BY_ID_QUERY, new FilmRowMapper(), filmId);

        assertThat(dbFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedFilm);
    }

    @Test
    @DisplayName("Должен вернуть все фильмы")
    void testGetAllFilms() {

        List<Film> dbFilms = jdbc.query(GET_ALL_FILMS_QUERY, new FilmRowMapper());
        Collection<Film> films = filmStorage.getAllFilms();
        assertThat(films).hasSize(dbFilms.size());

        dbFilms.forEach(dbFilm -> {
            Optional<Film> filmOpt = films.stream()
                    .filter(film -> film.getId().equals(dbFilm.getId()))
                    .findFirst();
            assertThat(filmOpt).isPresent();
            assertThat(filmOpt.get()).usingRecursiveComparison().isEqualTo(dbFilm);
        });
    }

}
