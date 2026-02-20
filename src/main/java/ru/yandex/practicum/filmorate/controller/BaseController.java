package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.BaseModel;

import java.util.Map;
import java.util.Optional;

public abstract class BaseController {

    protected abstract Logger getLogger();

    protected void handleValidationError(Optional<String> errorOpt) throws ValidationException {
        errorOpt.ifPresent(error -> {
            getLogger().error(error);
            throw new ValidationException(error);
        });
    }

    protected static long getNextId(Map<Long, ? extends BaseModel> data) {
        long currentMaxId = data.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
