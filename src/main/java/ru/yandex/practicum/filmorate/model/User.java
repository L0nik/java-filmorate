package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class User extends BaseModel {
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();

    public void validateEmail() throws ValidationException {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ '@'");
        }
    }

    public void validateLogin() throws ValidationException {
        if (login == null || login.isBlank() || login.contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
    }

    public void validateBirthday() throws ValidationException {
        if (birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    public void addFriend(long userId) {
        friends.add(userId);
    }

    public void deleteFriend(long friendId) {
        friends.remove(friendId);
    }
}
