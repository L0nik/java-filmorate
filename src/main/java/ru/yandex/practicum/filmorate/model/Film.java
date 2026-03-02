package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


/**
 * Film.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Film extends BaseModel {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private final Set<Long> likes = new HashSet<>();

    @Getter
    private static final int maxDescriptionLength = 200;

    @Getter
    private static final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    public void validateName() throws ValidationException {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
    }

    public void validateDescription() throws ValidationException {
        if (description != null && description.length() > maxDescriptionLength) {
            throw new ValidationException(String.format("Максимальная длина описания — %d символов", maxDescriptionLength));
        }
    }

    public void validateReleaseDate() throws ValidationException {
        if (releaseDate != null && releaseDate.isBefore(minReleaseDate)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String message = String.format("Дата релиза — не раньше %s", minReleaseDate.format(formatter));
            throw new ValidationException(message);
        }
    }

    public void validateDuration() throws ValidationException {
        if (duration != null && duration <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    public void addLike(long userId) {
        likes.add(userId);
    }

    public void removeLike(long userId) {
        likes.remove(userId);
    }
}
