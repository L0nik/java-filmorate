package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

    private static final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User getUserById(long id) throws NotFoundException {
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
    public User addUser(User newUser) throws ValidationException {
        log.info("Начало добавления пользователя {}", newUser);
        validateUser(newUser);
        newUser.setId(getNextId());
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь: {}", newUser);
        return newUser;
    }

    @Override
    public User updateUser(User newUser) throws ValidationException, NotFoundException {

        if (newUser.getId() == null) {
            String errorMessage = "Не указан id";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        User user = users.get(newUser.getId());
        if (user == null) {
            String errorMessage = String.format("Пользователь с id '%d' не найден", newUser.getId());
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }

        log.info("Начало обновления пользователя: {}", user);
        if (newUser.getEmail() != null) {
            newUser.validateEmail();
            user.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null) {
            newUser.validateLogin();
            user.setLogin(newUser.getLogin());
        }

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            user.setName(newUser.getName());
        } else {
            user.setName(user.getLogin());
        }

        if (newUser.getBirthday() != null) {
            newUser.validateBirthday();
            user.setBirthday(newUser.getBirthday());
        }
        log.info("Пользователь успешно обновлен: {}", user);

        return user;
    }

    private void validateUser(User user) throws ValidationException {
        log.info("Начало валидации пользователя {}", user);
        user.validateEmail();
        user.validateLogin();
        user.validateBirthday();
        log.info("Валидация пользователя завершилась успешно {}", user);
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
