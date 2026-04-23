package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class User extends BaseModel {

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Некорректный адрес электронной почты")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private final Set<User> friends = new HashSet<>();

    public void addFriend(User user) {
        friends.add(user);
    }

    public void deleteFriend(long friendId) {
        Optional<User> friendOpt = friends.stream()
                .filter(friend -> friend.getId() == friendId)
                .findFirst();
        friendOpt.ifPresent(friends::remove);
    }
}
