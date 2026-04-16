package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.util.Collection;

@Slf4j
@Repository
public class DbDirectorStorage extends DbBaseStorage<Director> implements DirectorStorage {

    private static final String GET_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM directors";
    private static final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE id = ?";

    public DbDirectorStorage(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Director getDirectorById(long id) {
        log.debug("Выполнение запроса на поиск режиссёра по id: {}", id);
        return findOne(GET_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    @Override
    public Collection<Director> getAllDirectors() {
        log.debug("Выполнение запроса на получение всех режиссёров");
        return findMany(GET_ALL_QUERY);
    }

    @Override
    public Director addDirector(Director newDirector) {
        log.debug("Выполнение запроса на добавление режиссёра: имя = {}", newDirector.getName());
        Long id = insert(INSERT_QUERY, newDirector.getName());
        newDirector.setId(id);
        log.info("Успешно добавлен новый режиссёр: id = {}, имя = {}", id, newDirector.getName());
        return newDirector;
    }

    @Override
    public void updateDirector(Director director) {
        log.debug("Выполнение запроса на обновление режиссёра: id = {}, новое имя = {}",
                director.getId(), director.getName());
        update(UPDATE_QUERY, director.getName(), director.getId());
        log.info("Успешно обновлён режиссёр: id = {}, имя = {}", director.getId(), director.getName());
    }

    @Override
    public void deleteDirector(long id) {
        log.debug("Выполнение запроса на удаление режиссёра с id: {}", id);
        update(DELETE_QUERY, id);
        log.info("Успешно удалён режиссёр с id: {}", id);
    }

    @Override
    public void checkIfDirectorExists(long id) {
        getDirectorById(id);
    }
}