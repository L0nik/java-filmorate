package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@Slf4j
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director getDirectorById(long id) {
        return directorStorage.getDirectorById(id);
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director addDirector(Director newDirector) {
        return directorStorage.addDirector(newDirector);
    }

    public Director updateDirector(Director newDirector) {
        if (newDirector.getId() == null) {
            String errorMessage = "Не указан id";
            log.error(errorMessage);
            throw new ValidationException(errorMessage);
        }

        Director director = directorStorage.getDirectorById(newDirector.getId());

        log.info("Начало обновления режиссера: {}", director);
        if (newDirector.getName() != null) {
            director.setName(newDirector.getName());
        }

        directorStorage.updateDirector(director);

        log.info("Режиссер успешно обновлен: {}", director);

        return director;
    }

    public void deleteDirector(long id) {
        directorStorage.checkIfDirectorExists(id);
        directorStorage.deleteDirector(id);
    }
}
