package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


/**
 * Film.
 */
@Data
public class Film extends BaseModel {
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;

    @Getter
    private static final int maxDescriptionLength = 200;

    @Getter
    private static final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    public Optional<String> validateName() {
        if (name == null || name.isBlank()) {
            return Optional.of("Название не может быть пустым");
        }
        return Optional.empty();
    }

    public Optional<String> validateDescription() {
        if (description != null && description.length() > maxDescriptionLength) {
            return Optional.of(String.format("Максимальная длина описания — %d символов", maxDescriptionLength));
        }
        return Optional.empty();
    }

    public Optional<String> validateReleaseDate() {
        if (releaseDate != null && releaseDate.isBefore(minReleaseDate)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String message = String.format("Дата релиза — не раньше %s", minReleaseDate.format(formatter));
            return Optional.of(message);
        }
        return Optional.empty();
    }

    public Optional<String> validateDuration() {
        if (duration != null && duration <= 0) {
            return Optional.of("Продолжительность фильма должна быть положительным числом");
        }
        return Optional.empty();
    }
}
