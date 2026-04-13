package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DbUserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DbUserStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbUserStorageTests {
    private final DbUserStorage userStorage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    public void beforeEach() {
        jdbc.update("DELETE FROM users");
        jdbc.update("INSERT INTO users (id, email, login, name, birthday) VALUES (1, 'test@test.ru', 'test login', 'test name', '2000-01-01')");
        jdbc.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 2");
    }

    @Test
    @DisplayName("Должен вернуться пользователь, если он существует")
    public void testFindUserByIdPositive() {

        User user = userStorage.getUserById(1);

        assertThat(user)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("email", "test@test.ru");
    }

    @Test
    @DisplayName("Должен выбросить NotFoundException, если пользователя нет")
    void testFindUserByIdNegative() {
        long nonExistentId = 999L;

        assertThatThrownBy(() -> userStorage.getUserById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id '" + nonExistentId + "' не найден");
    }

    @Test
    @DisplayName("Должен успешно сохранить пользователя и вернуть его")
    void testAddUser() {
        User newUser = new User();
        newUser.setEmail("newtest@test.ru");
        newUser.setLogin("new test login");
        newUser.setName("new test name");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));

        User savedUser = userStorage.addUser(newUser);

        assertThat(savedUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "newtest@test.ru")
                .hasFieldOrProperty("id");

        assertThat(savedUser.getId()).isPositive();

        String sql = "SELECT * FROM users WHERE id = ?";
        User userFromDb = jdbc.queryForObject(sql, new UserRowMapper(), savedUser.getId());

        assertThat(userFromDb)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedUser);
    }

    @Test
    @DisplayName("Должен успешно обновить пользователя")
    void testUpdateUser() {

        long userId = 1L;
        User userBefore = jdbc.queryForObject("SELECT * FROM users WHERE id = ?", new UserRowMapper(), userId);
        assertThat(userBefore).isNotNull();

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail(userBefore.getEmail());
        updatedUser.setLogin(userBefore.getLogin());
        updatedUser.setName(userBefore.getName() + " updated");
        updatedUser.setBirthday(userBefore.getBirthday().plusMonths(1).plusDays(1));

        userStorage.updateUser(updatedUser);

        User dbUser = jdbc.queryForObject("SELECT * FROM users WHERE id = ?", new UserRowMapper(), 1L);

        assertThat(dbUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedUser);
    }

    @Test
    @DisplayName("Должен вернуть всех существующих пользователей")
    void testGetAllUsers() {

        List<User> dbUsers = jdbc.query("SELECT * FROM users", new UserRowMapper());
        Collection<User> users = userStorage.getAllUsers();
        assertThat(users).hasSize(dbUsers.size());

        dbUsers.forEach(dbUser -> {
            Optional<User> userOpt = users.stream()
                    .filter(user -> user.getId().equals(dbUser.getId()))
                    .findFirst();
            assertThat(userOpt).isPresent();
            assertThat(userOpt.get()).usingRecursiveComparison().isEqualTo(dbUser);
        });
    }
}
