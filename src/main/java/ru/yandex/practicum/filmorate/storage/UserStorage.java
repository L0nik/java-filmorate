package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    User getUserById(long id);

    User addUser(User newUser);

    void updateUser(User newUser);

    Collection<User> getAllUsers();

    void checkIfUserExists(long id);

    Collection<User> getCommonFriends(long userId1, long userId2);

    Collection<User> getFriendsOfUser(long userId);

    void deleteUser(long id);

    Optional<Long> findMostSimilarUserId(Long userId);

    Set<Long> getRecommendedFilmIds(Long userId, Long similarUserId);
}