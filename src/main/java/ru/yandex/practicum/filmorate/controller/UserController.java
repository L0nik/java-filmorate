package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    private final static Consumer<Optional<String>> validationErrorConsumer = (errorOpt) -> {
        if (errorOpt.isPresent()) {
            log.error(errorOpt.get());
            throw new ValidationException(errorOpt.get());
        }
    };

    @PostMapping
    public User addUser(@RequestBody User newUser) throws ValidationException {
        log.info("Получен запрос на добавление пользователя: {}", newUser);
        validateUser(newUser);
        newUser.setId(getNextId());
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь: {}", newUser);
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {

        log.info("Получен запрос на обновление пользователя: {}", newUser);

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
            validationErrorConsumer.accept(newUser.validateEmail());
            user.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null) {
            validationErrorConsumer.accept(newUser.validateLogin());
            user.setLogin(newUser.getLogin());
        }

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            user.setName(newUser.getName());
        } else {
            user.setName(user.getLogin());
        }

        if (newUser.getBirthday() != null) {
            validationErrorConsumer.accept(newUser.validateBirthday());
            user.setBirthday(newUser.getBirthday());
        }
        log.info("Пользователь успешно обновлен: {}", user);

        return user;
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }

    public void validateUser(User user) throws ValidationException {
        validationErrorConsumer.accept(user.validateEmail());
        validationErrorConsumer.accept(user.validateLogin());
        validationErrorConsumer.accept(user.validateBirthday());
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
