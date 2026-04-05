package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository("dbUserStorage")
@Slf4j
public class DbUserStorage extends DbBaseStorage<User> implements UserStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday)" +
            " VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    public DbUserStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User getUserById(long id) {
        Optional<User> userOpt = findOne(FIND_BY_ID_QUERY, id);
        if (userOpt.isEmpty()) {
            String errorMessage = String.format("Пользователь с id '%d' не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return userOpt.get();
    }

    @Override
    public User addUser(User newUser) {
        log.info("Начало добавления пользователя {}", newUser);
        Long id = insert(
                INSERT_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday()
        );
        newUser.setId(id);
        log.info("Добавлен новый пользователь: {}", newUser);
        return newUser;
    }

    @Override
    public void updateUser(User newUser) {
        update(
                UPDATE_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId()
        );
    }

    @Override
    public Collection<User> getAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public void checkIfUserExists(long id) {
        getUserById(id);
    }
}
