package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User newUser) throws ValidationException {
        return userStorage.addUser(newUser);
    }

    public User updateUser(User newUser)  throws ValidationException, NotFoundException {
        return userStorage.updateUser(newUser);
    }


    public void addFriend(long userId, long friendId) throws NotFoundException {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.addFriend(friendId);
        friend.addFriend(friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) throws NotFoundException {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.deleteFriend(friendId);
        friend.deleteFriend(userId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public Collection<User> getListOfCommonFriends(long userId1, long userId2) throws NotFoundException {
        User user1 = userStorage.getUserById(userId1);
        User user2 = userStorage.getUserById(userId2);
        HashSet<Long> commonFriends = new HashSet<>(user1.getFriends());
        commonFriends.retainAll(user2.getFriends());
        return commonFriends.stream()
                .map(userStorage::getUserById)
                .toList();
    }
}
