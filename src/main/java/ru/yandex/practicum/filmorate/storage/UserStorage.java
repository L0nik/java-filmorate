package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    public User getUserById(long id);
    public User addUser(User newUser);
    public User updateUser(User newUser);
    public Collection<User> getAllUsers();
}
