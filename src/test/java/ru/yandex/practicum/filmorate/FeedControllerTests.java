package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:feedtestdb;CASE_INSENSITIVE_IDENTIFIERS=TRUE"
)
public class FeedControllerTests {
    @LocalServerPort
    private int port;

    private static HttpClient client;
    private static Gson gson;
    private static int userCount = 0;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    @Test
    @DisplayName("GET /users/{id}/feed - показывает событие добавления в друзья от друга")
    void getFeedShouldReturnFriendEventsOfFriends() throws Exception {
        User viewer = createValidUser();
        User actor = createValidUser();
        User thirdUser = createValidUser();

        postValidUser(viewer);
        postValidUser(actor);
        postValidUser(thirdUser);

        assertEquals(200, sendAddFriend(viewer.getId(), actor.getId()).statusCode());
        assertEquals(200, sendAddFriend(actor.getId(), thirdUser.getId()).statusCode());

        HttpResponse<String> feedResponse = sendGetFeed(viewer.getId());
        assertEquals(200, feedResponse.statusCode());

        JsonArray events = gson.fromJson(feedResponse.body(), JsonArray.class);
        assertEquals(1, events.size());

        JsonObject event = events.get(0).getAsJsonObject();
        assertEquals(actor.getId(), event.get("userId").getAsLong());
        assertEquals("FRIEND", event.get("eventType").getAsString());
        assertEquals("ADD", event.get("operation").getAsString());
        assertEquals(thirdUser.getId(), event.get("entityId").getAsLong());
    }

    @Test
    @DisplayName("GET /users/{id}/feed - возвращает события лайка и отзыва друга")
    void getFeedShouldReturnLikeAndReviewEvents() throws Exception {
        User viewer = createValidUser();
        User actor = createValidUser();
        postValidUser(viewer);
        postValidUser(actor);

        Film film = createValidFilm();
        HttpResponse<String> filmResponse = sendFilmPost(gson.toJson(film));
        assertEquals(200, filmResponse.statusCode());
        Film createdFilm = gson.fromJson(filmResponse.body(), Film.class);

        assertEquals(200, sendAddFriend(viewer.getId(), actor.getId()).statusCode());
        assertEquals(200, sendPutLike(createdFilm.getId(), actor.getId()).statusCode());

        Review review = new Review();
        review.setContent("good movie");
        review.setIsPositive(true);
        review.setUserId(actor.getId());
        review.setFilmId(createdFilm.getId());

        HttpResponse<String> reviewResponse = sendReviewPost(gson.toJson(review));
        assertEquals(200, reviewResponse.statusCode());

        HttpResponse<String> feedResponse = sendGetFeed(viewer.getId());
        assertEquals(200, feedResponse.statusCode());

        JsonArray events = gson.fromJson(feedResponse.body(), JsonArray.class);
        assertEquals(2, events.size());

        assertTrue(
                events.get(0).getAsJsonObject().get("timestamp").getAsLong()
                        >= events.get(1).getAsJsonObject().get("timestamp").getAsLong()
        );

        assertEquals("REVIEW", events.get(0).getAsJsonObject().get("eventType").getAsString());
        assertEquals("ADD", events.get(0).getAsJsonObject().get("operation").getAsString());
        assertEquals("LIKE", events.get(1).getAsJsonObject().get("eventType").getAsString());
        assertEquals("ADD", events.get(1).getAsJsonObject().get("operation").getAsString());
    }

    private User createValidUser() {
        userCount++;
        User user = new User();
        user.setEmail("feeduser" + userCount + "@test.com");
        user.setLogin("feedlogin" + userCount);
        user.setName("feed name " + userCount);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private Film createValidFilm() {
        FilmRating mpa = new FilmRating();
        mpa.setId(2L);
        mpa.setName("PG");

        Film film = new Film();
        film.setName("feed film");
        film.setDescription("feed film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        return film;
    }

    private void postValidUser(User user) throws IOException, InterruptedException {
        HttpResponse<String> response = sendUserPost(gson.toJson(user));
        assertEquals(200, response.statusCode(), response.body());
        User createdUser = gson.fromJson(response.body(), User.class);
        user.setId(createdUser.getId());
    }

    private HttpResponse<String> sendUserPost(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getUsersUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendFilmPost(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getFilmsUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendReviewPost(String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getReviewsUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendAddFriend(long userId, long friendId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getUsersUrl() + "/" + userId + "/friends/" + friendId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPutLike(long filmId, long userId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getFilmsUrl() + "/" + filmId + "/like/" + userId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGetFeed(long userId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getUsersUrl() + "/" + userId + "/feed"))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String getUsersUrl() {
        return "http://localhost:" + port + "/users";
    }

    private String getFilmsUrl() {
        return "http://localhost:" + port + "/films";
    }

    private String getReviewsUrl() {
        return "http://localhost:" + port + "/reviews";
    }
}
