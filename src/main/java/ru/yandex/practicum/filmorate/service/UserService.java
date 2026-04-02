package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(
            @Qualifier("dbUserStorage") UserStorage userStorage,
            FriendshipStorage friendshipStorage
    ) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public User getUserById(long id) {
        User user = userStorage.getUserById(id);
        friendshipStorage.getFriendsOfUser(id).forEach(friendship -> {
            user.addFriend(friendship.getFriendId());
        });
        return user;
    }

    public Collection<User> getAllUsers() {
        Collection<User> users = userStorage.getAllUsers();
        users.forEach(user -> {
            friendshipStorage.getFriendsOfUser(user.getId()).forEach(friendship -> {
                user.addFriend(friendship.getFriendId());
            });
        });
        return users;
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
        friendshipStorage.addFriend(userId, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        friendshipStorage.deleteFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public Collection<User> getFriendsOfUser(long userId) {
        return friendshipStorage.getFriendsOfUser(userId).stream()
                .map(friendship -> userStorage.getUserById(friendship.getUserId()))
                .toList();
    }

    public Collection<User> getCommonFriends(long userId1, long userId2) {
        List<Long> friendsIds1 = friendshipStorage.getFriendsOfUser(userId1).stream()
                .map(Friendship::getFriendId)
                .toList();
        List<Long> friendsIds2 = friendshipStorage.getFriendsOfUser(userId2).stream()
                .map(Friendship::getFriendId)
                .toList();
        HashSet<Long> commonFriends = new HashSet<>(friendsIds1);
        commonFriends.retainAll(friendsIds2);
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
