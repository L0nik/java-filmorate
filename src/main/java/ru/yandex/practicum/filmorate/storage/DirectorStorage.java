package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {
    Collection<Director> getAllDirectors();

    Director getDirectorById(long id);

    Director addDirector(Director newDirector);

    void deleteDirector(long id);

    void checkIfDirectorExists(long id);

    void updateDirector(Director director);
}
