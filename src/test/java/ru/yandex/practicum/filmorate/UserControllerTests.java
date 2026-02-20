package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerTests {
    private static final String BASE_URL = "http://localhost:8080/users";
    private static HttpClient client;
    private static Gson gson;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    @Test
    @DisplayName("POST /users - валидный пользователь -> 200")
    void PostShouldCreateUserWhenValid() throws Exception {

        User user = createValidUser();

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(200, response.statusCode());

        User responseUser = gson.fromJson(response.body(), User.class);

        assertEquals(user.getEmail(), responseUser.getEmail());
        assertEquals(user.getLogin(), responseUser.getLogin());
        assertEquals(user.getName(), responseUser.getName());
        assertEquals(user.getBirthday(), responseUser.getBirthday());
    }

    @Test
    @DisplayName("POST /users - валидный пользователь (пустое имя) -> 200")
    void PostShouldCreateUserWhenValidWhenNameIsBlank() throws Exception {

        User user = createValidUser();
        user.setName("");

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(200, response.statusCode());

        User responseUser = gson.fromJson(response.body(), User.class);

        assertEquals(user.getEmail(), responseUser.getEmail());
        assertEquals(user.getLogin(), responseUser.getLogin());
        assertEquals(user.getLogin(), responseUser.getName());
        assertEquals(user.getBirthday(), responseUser.getBirthday());
    }

    @Test
    @DisplayName("POST /users - пустой email -> 500")
    void PostShouldReturn500WhenEmailIsBlank() throws Exception {

        User user = createValidUser();
        user.setEmail("");

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("POST /users - email отсутствует -> 500")
    void PostShouldReturn500WhenEmailIsNull() throws Exception {

        User user = createValidUser();
        user.setEmail(null);

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("POST /users - email не содержит @ -> 500")
    void PostShouldReturn500WhenEmailHasNoAtSign() throws Exception {

        User user = createValidUser();
        user.setEmail("testemail.com");

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("POST /users - пустой логин -> 500")
    void PostShouldReturn500WhenLoginIsBlank() throws Exception {

        User user = createValidUser();
        user.setLogin("");

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("POST /users - отсутствует логин -> 500")
    void PostShouldReturn500WhenLoginIsNull() throws Exception {

        User user = createValidUser();
        user.setLogin(null);

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("POST /users - в логине есть пробелы -> 500")
    void PostShouldReturn500WhenLoginHasWhitespaces() throws Exception {

        User user = createValidUser();
        user.setLogin("test login");

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("POST /users - дата рождения в будущем -> 500")
    void PostShouldReturn500WhenBirthdayInFuture() throws Exception {

        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("POST /users - дата рождения сегодня -> 200")
    void PostShouldCreateUserWhenBirthdayIsToday() throws Exception {

        User user = createValidUser();
        user.setBirthday(LocalDate.now());

        String json = gson.toJson(user);

        HttpResponse<String> response = sendPost(json);

        assertEquals(200, response.statusCode());

        User responseUser = gson.fromJson(response.body(), User.class);
        assertEquals(LocalDate.now(), responseUser.getBirthday());
    }

    @Test
    @DisplayName("POST /users - отсутствует тело запроса -> 400")
    void PostShouldReturn400WhenBodyIsAbsent() throws Exception {
        HttpResponse<String> response = sendPost("");
        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("PUT /users - валидное обновление пользователя -> 200")
    void PutShouldUpdateUserWhenValid() throws Exception {

        User user = createValidUser();
        HttpResponse<String> response = sendPost(gson.toJson(user));
        assertEquals(200, response.statusCode());
        User createdUser = gson.fromJson(response.body(), User.class);
        assertEquals(user.getEmail(), createdUser.getEmail());

        user.setId(createdUser.getId());
        user.setEmail("updatedemail@test.com");
        response = sendPut(gson.toJson(user));
        User responseUser = gson.fromJson(response.body(), User.class);
        assertEquals(user.getEmail(), responseUser.getEmail());
    }

    @Test
    @DisplayName("PUT /users - валидное обновление пользователя (пустое имя) -> 200")
    void PutShouldUpdateUserWhenValidAndNameIsBlank() throws Exception {

        User user = createValidUser();
        HttpResponse<String> response = sendPost(gson.toJson(user));
        assertEquals(200, response.statusCode());
        User createdUser = gson.fromJson(response.body(), User.class);
        assertEquals(user.getName(), createdUser.getName());

        user.setId(createdUser.getId());
        user.setName("");
        response = sendPut(gson.toJson(user));
        User responseUser = gson.fromJson(response.body(), User.class);
        assertEquals(user.getLogin(), responseUser.getName());
    }

    @Test
    @DisplayName("PUT /users - отсутствует id -> 500")
    void PutShouldReturn500WhenIdMissing() throws Exception {

        User user = createValidUser();
        user.setId(null);

        String json = gson.toJson(user);
        HttpResponse<String> response = sendPut(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("PUT /users - не существующий id -> 500")
    void PutShouldReturn500WhenIdNotExists() throws Exception {

        User user = createValidUser();
        user.setId(9999L);

        String json = gson.toJson(user);
        HttpResponse<String> response = sendPut(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("PUT /users - пустой email -> 500")
    void PutShouldReturn500WhenEmailIsBlank() throws Exception {

        User user = createValidUser();
        user.setEmail("");

        String json = gson.toJson(user);
        HttpResponse<String> response = sendPut(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("PUT /users - email не содержит знак @ -> 500")
    void PutShouldReturn500WhenEmailHasNoAtSign() throws Exception {

        User user = createValidUser();
        user.setEmail("testemail.com");

        String json = gson.toJson(user);
        HttpResponse<String> response = sendPut(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("PUT /users - пустой логин -> 500")
    void PutShouldReturn500WhenLoginIsBlank() throws Exception {

        User user = createValidUser();
        user.setLogin("");

        String json = gson.toJson(user);
        HttpResponse<String> response = sendPut(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("PUT /users - логин содержит пробелы -> 500")
    void PutShouldReturn500WhenLoginHasWhitespaces() throws Exception {

        User user = createValidUser();
        user.setLogin("test login");

        String json = gson.toJson(user);
        HttpResponse<String> response = sendPut(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("PUT /users - дата рождения в будущем -> 500")
    void PutShouldReturn500WhenBirthdayInFuture() throws Exception {

        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        String json = gson.toJson(user);
        HttpResponse<String> response = sendPut(json);

        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("PUT /users - дата рождения сегодня -> 200")
    void PutShouldUpdateUserWhenBirthdayIsToday() throws Exception {

        User user = createValidUser();
        HttpResponse<String> response = sendPost(gson.toJson(user));
        assertEquals(200, response.statusCode());
        User createdUser = gson.fromJson(response.body(), User.class);
        assertEquals(user.getBirthday(), createdUser.getBirthday());

        user.setId(createdUser.getId());
        user.setBirthday(LocalDate.now());
        response = sendPut(gson.toJson(user));
        User responseUser = gson.fromJson(response.body(), User.class);
        assertEquals(user.getBirthday(), responseUser.getBirthday());
    }

    @Test
    @DisplayName("PUT /users - тело отсутствует -> 400")
    void PutShouldReturn400WhenBodyIsAbsent() throws Exception {
        HttpResponse<String> response = sendPut("");
        assertEquals(400, response.statusCode());
    }

    private User createValidUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("testemail@test.com");
        user.setLogin("testlogin");
        user.setName("test name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private HttpResponse<String> sendPost(String json)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPut(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
