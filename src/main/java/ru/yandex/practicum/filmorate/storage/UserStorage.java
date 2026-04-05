package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User getUserById(long id);

    User addUser(User newUser);

    void updateUser(User newUser);

    Collection<User> getAllUsers();

    void checkIfUserExists(long id);
}
