package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User newUser) {
        validateUser(newUser);
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        return userStorage.addUser(newUser);
    }

    public User updateUser(User newUser) {

        if (newUser.getId() == null) {
            String errorMessage = "Не указан id";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        User user = userStorage.getUserById(newUser.getId());

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

        userStorage.updateUser(user);

        log.info("Пользователь успешно обновлен: {}", user);

        return user;
    }


    public void addFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.addFriend(friendId);
        friend.addFriend(userId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.deleteFriend(friendId);
        friend.deleteFriend(userId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public Collection<User> getFriendsOfUser(long userId) {
        User user = userStorage.getUserById(userId);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .toList();
    }

    public Collection<User> getCommonFriends(long userId1, long userId2) {
        User user1 = userStorage.getUserById(userId1);
        User user2 = userStorage.getUserById(userId2);
        HashSet<Long> commonFriends = new HashSet<>(user1.getFriends());
        commonFriends.retainAll(user2.getFriends());
        return commonFriends.stream()
                .map(userStorage::getUserById)
                .toList();
    }

    private void validateUser(User user) {
        log.info("Начало валидации пользователя {}", user);
        user.validateEmail();
        user.validateLogin();
        user.validateBirthday();
        log.info("Валидация пользователя завершилась успешно {}", user);
    }
}
