package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@Data
@EqualsAndHashCode
@ToString
public class Review {
    Long reviewId;
    String content;
    Boolean isPositive;
    Long userId;
    Long filmId;
    int useful;

    public void validateContent() {
        if (content == null) {
            throw new ValidationException("В отзыве отсутствует контент");
        }
    }

    public void validateUserId() {
        if (userId == null) {
            throw new ValidationException("Не указан id пользователя, оставившего отзыв");
        }
    }

    public void validateFilmId() {
        if (filmId == null) {
            throw new ValidationException("Не указан id фильма, к которому относится отзыв");
        }
    }

    public void validateIsPositive() {
        if (isPositive == null) {
            throw new ValidationException("Не указан тип отзыва (положительный/отрицательный)");
        }
    }
}
