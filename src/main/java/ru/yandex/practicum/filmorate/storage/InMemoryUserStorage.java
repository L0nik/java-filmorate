package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User getUserById(long id) {
        User user = users.get(id);
        if (user == null) {
            String errorMessage = String.format("Пользователь с id '%d' не найден", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public User addUser(User newUser) {
        log.info("Начало добавления пользователя {}", newUser);
        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь: {}", newUser);
        return newUser;
    }

    @Override
    public void updateUser(User newUser) {
        users.put(newUser.getId(), newUser);
    }

    protected long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
