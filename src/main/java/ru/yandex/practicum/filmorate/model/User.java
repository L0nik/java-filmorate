package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Optional;

@Data
public class User extends BaseModel {
    String email;
    String login;
    String name;
    LocalDate birthday;

    public Optional<String> validateEmail() {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return Optional.of("Электронная почта не может быть пустой и должна содержать символ '@'");
        }
        return Optional.empty();
    }

    public Optional<String> validateLogin() {
        if (login == null || login.isBlank() || login.contains(" ")) {
            return Optional.of("Логин не может быть пустым и содержать пробелы");
        }
        return Optional.empty();
    }

    public Optional<String> validateBirthday() {
        if (birthday.isAfter(LocalDate.now())) {
            return Optional.of("Дата рождения не может быть в будущем");
        }
        return Optional.empty();
    }
}
