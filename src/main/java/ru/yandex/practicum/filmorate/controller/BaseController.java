package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.BaseModel;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class BaseController {
    private final static Logger log = LoggerFactory.getLogger(BaseController.class);

    protected final static Consumer<Optional<String>> validationErrorConsumer = (errorOpt) -> {
        if (errorOpt.isPresent()) {
            log.error(errorOpt.get());
            throw new ValidationException(errorOpt.get());
        }
    };

    protected static long getNextId(Map<Long, ? extends BaseModel> data) {
        long currentMaxId = data.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
