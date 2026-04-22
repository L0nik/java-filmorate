package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final EventStorage eventStorage;
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    public UserService(
            @Qualifier("dbUserStorage") UserStorage userStorage,
            FriendshipStorage friendshipStorage,
            EventStorage eventStorage,
            FilmStorage filmStorage,
            GenreStorage genreStorage,
            DirectorStorage directorStorage
    ) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.eventStorage = eventStorage;
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
    }

    public User getUserById(long id) {
        User user = userStorage.getUserById(id);
        friendshipStorage.getFriendsOfUser(id).forEach(friendship -> {
            user.addFriend(userStorage.getUserById(friendship.getFriendId()));
        });
        return user;
    }

    public Collection<User> getAllUsers() {
        Collection<User> users = userStorage.getAllUsers();
        users.forEach(user -> {
            friendshipStorage.getFriendsOfUser(user.getId()).forEach(friendship -> {
                user.addFriend(userStorage.getUserById(friendship.getFriendId()));
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
        userStorage.checkIfUserExists(userId);
        userStorage.checkIfUserExists(friendId);
        friendshipStorage.addFriend(userId, friendId);
        eventStorage.addEvent(userId, "FRIEND", "ADD", friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        userStorage.checkIfUserExists(userId);
        userStorage.checkIfUserExists(friendId);
        if (friendshipStorage.checkIfUserHasFriend(userId, friendId)) {
            friendshipStorage.deleteFriend(userId, friendId);
            eventStorage.addEvent(userId, "FRIEND", "REMOVE", friendId);
            log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        } else {
            log.info("У пользователя {} нет в друзьях пользователя {}", userId, friendId);
        }
    }

    public Collection<User> getFriendsOfUser(long userId) {
        userStorage.checkIfUserExists(userId);
        return userStorage.getFriendsOfUser(userId);
    }

    public Collection<User> getCommonFriends(long userId1, long userId2) {
        return userStorage.getCommonFriends(userId1, userId2);
    }

    public void deleteUser(long id) {
        userStorage.deleteUser(id);
        log.info("Пользователь {} удален", id);
    }

    public Collection<Event> getFeed(long id) {
        userStorage.checkIfUserExists(id);
        return eventStorage.getFeed(id);
    }

    public List<Film> getRecommendations(Long userId) {

        if (userId == null || userId <= 0) {
            throw new ValidationException("Некорректный userId");
        }

        userStorage.checkIfUserExists(userId);

        Optional<Long> similarUserId =
                userStorage.findMostSimilarUserId(userId);

        if (similarUserId.isEmpty()) {
            return List.of();
        }

        Set<Long> filmIds =
                userStorage.getRecommendedFilmIds(
                        userId,
                        similarUserId.get()
                );

        List<Film> films = filmIds.stream()
                .map(filmStorage::getFilmById)
                .toList();
        films.forEach(film -> {
            film.addGenres(genreStorage.getGenresByFilmId(film.getId()));
            film.setDirectors(directorStorage.getDirectorsByFilmId(film.getId()));
        });

        return films;
    }

    private void validateUser(User user) {
        log.info("Начало валидации пользователя {}", user);
        user.validateEmail();
        user.validateLogin();
        user.validateBirthday();
        log.info("Валидация пользователя завершилась успешно {}", user);
    }
}
