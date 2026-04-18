package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class FilmControllerTests {
    private static final String BASE_URL = "http://localhost:8080/films";
    private static HttpClient client;
    private static Gson gson;
    private static final LocalDate minReleaseDate = Film.getMinReleaseDate();
    private static final int maxDescriptionLength = Film.getMaxDescriptionLength();

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    @Test
    @DisplayName("GET /films -> 200 OK")
    void getShouldReturn200WhenGetFilms() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    @DisplayName("POST /films - валидный фильм -> 200")
    void postShouldCreateFilmWhenValid() throws Exception {

        Film film = createValidFilm();

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(200, response.statusCode());

        Film responseFilm = gson.fromJson(response.body(), Film.class);

        assertEquals(film.getName(), responseFilm.getName());
        assertEquals(film.getDuration(), responseFilm.getDuration());
    }

    @Test
    @DisplayName("POST /films - пустое название -> 400")
    void postShouldReturn400WhenNameIsBlank() throws Exception {

        Film film = createValidFilm();
        film.setName("");

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("POST /films - отсутствует название -> 400")
    void postShouldReturn400WhenNameIsNull() throws Exception {

        Film film = createValidFilm();
        film.setName(null);

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("POST /films - длина описания равна максимальной длине -> 200 OK")
    void postShouldCreateFilmWhenDescriptionIs200Chars() throws Exception {

        Film film = createValidFilm();
        film.setDescription("a".repeat(maxDescriptionLength));

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(200, response.statusCode());

        Film responseFilm = gson.fromJson(response.body(), Film.class);
        assertEquals(maxDescriptionLength, responseFilm.getDescription().length());
    }

    @Test
    @DisplayName("POST /films - длина описания больше максимальной -> 400")
    void postShouldReturn400WhenDescriptionTooLong() throws Exception {

        Film film = createValidFilm();
        film.setDescription("a".repeat(maxDescriptionLength + 1));

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("POST /films - дата релиза = минимальная дата релиза -> 200 OK")
    void postShouldCreateFilmWhenReleaseDateIsBoundary() throws Exception {

        Film film = createValidFilm();
        film.setReleaseDate(minReleaseDate);

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(200, response.statusCode());

        Film responseFilm = gson.fromJson(response.body(), Film.class);
        assertEquals(minReleaseDate, responseFilm.getReleaseDate());
    }

    @Test
    @DisplayName("POST /films - releaseDate раньше минимальной даты релиза -> 400")
    void postShouldReturn400WhenReleaseDateTooEarly() throws Exception {

        Film film = createValidFilm();
        film.setReleaseDate(minReleaseDate.minusDays(1));

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("POST /films - продолжительность фильма < 0 -> 400")
    void postShouldReturn400WhenDurationNegative() throws Exception {

        Film film = createValidFilm();
        film.setDuration(-10);

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("POST /films - продолжительность фильма = 0 -> 400")
    void postShouldReturn400WhenDurationIs0() throws Exception {

        Film film = createValidFilm();
        film.setDuration(0);

        String json = gson.toJson(film);

        HttpResponse<String> response = sendPost(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("POST /films - пустое тело -> 500")
    void postShouldReturn500WhenEmptyBody() throws IOException, InterruptedException {
        HttpResponse<String> response = sendPost("");
        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("PUT /films - валидное обновление -> 200 OK")
    void putShouldUpdateFilmWhenValid() throws Exception {

        Film film = createValidFilm();

        String jsonPost = gson.toJson(film);
        HttpResponse<String> postResponse = sendPost(jsonPost);
        assertEquals(200, postResponse.statusCode());
        Film createdFilm = gson.fromJson(postResponse.body(), Film.class);

        film.setName("test name updated");
        film.setId(createdFilm.getId());
        String jsonPut = gson.toJson(film);

        HttpResponse<String> putResponse = sendPut(jsonPut);
        assertEquals(200, putResponse.statusCode());

        Film responseFilm = gson.fromJson(putResponse.body(), Film.class);
        assertEquals("test name updated", responseFilm.getName());
    }

    @Test
    @DisplayName("PUT /films - отсутствует id -> 400")
    void putShouldReturn400WhenIdMissing() throws Exception {

        Film film = createValidFilm();
        postValidFilm(film);

        film.setId(null);

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("PUT /films - не существующий id -> 404")
    void putShouldReturn404WhenIdNotExists() throws Exception {

        Film film = createValidFilm();
        postValidFilm(film);

        film.setId(9999L);

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("PUT /films - пустое название -> 400")
    void putShouldReturn400WhenNameIsBlank() throws Exception {

        Film film = createValidFilm();
        postValidFilm(film);

        film.setName("");

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("PUT /films - длина описания больше максимальной -> 400")
    void putShouldReturn400WhenDescriptionTooLong() throws Exception {

        Film film = createValidFilm();
        postValidFilm(film);

        film.setDescription("a".repeat(maxDescriptionLength + 1));

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("PUT /films - длина описания больше максимальной -> 200")
    void putShouldReturn500WhenDescriptionLengthIsBoundary() throws Exception {

        Film film = createValidFilm();

        String jsonPost = gson.toJson(film);
        HttpResponse<String> postResponse = sendPost(jsonPost);
        assertEquals(200, postResponse.statusCode());
        Film createdFilm = gson.fromJson(postResponse.body(), Film.class);

        film.setDescription("a".repeat(maxDescriptionLength));
        film.setId(createdFilm.getId());

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(200, response.statusCode());
        Film responseFilm = gson.fromJson(response.body(), Film.class);
        assertEquals(maxDescriptionLength, responseFilm.getDescription().length());

    }

    @Test
    @DisplayName("PUT /films - releaseDate равна минимальной дате релиза -> 500")
    void putShouldUpdateFilmWhenReleaseDateIsBoundary() throws Exception {

        Film film = createValidFilm();

        String jsonPost = gson.toJson(film);
        HttpResponse<String> postResponse = sendPost(jsonPost);
        assertEquals(200, postResponse.statusCode());
        Film createdFilm = gson.fromJson(postResponse.body(), Film.class);
        assertNotEquals(minReleaseDate, createdFilm.getReleaseDate());

        film.setReleaseDate(minReleaseDate);
        film.setId(createdFilm.getId());

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(200, response.statusCode());

        Film responseFilm = gson.fromJson(response.body(), Film.class);
        assertEquals(minReleaseDate, responseFilm.getReleaseDate());
    }

    @Test
    @DisplayName("PUT /films - releaseDate раньше минимальной даты релиза -> 400")
    void putShouldReturn400WhenReleaseDateTooEarly() throws Exception {

        Film film = createValidFilm();
        postValidFilm(film);

        film.setReleaseDate(minReleaseDate.minusDays(1));

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("PUT /films - продолжительность < 0 -> 400")
    void putShouldReturn400WhenDurationNegative() throws Exception {

        Film film = createValidFilm();
        postValidFilm(film);

        film.setDuration(-100);

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("PUT /films - продолжительность = 0 -> 400")
    void putShouldReturn400WhenDurationIs0() throws Exception {

        Film film = createValidFilm();
        postValidFilm(film);

        film.setDuration(-100);

        String json = gson.toJson(film);
        HttpResponse<String> response = sendPut(json);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("PUT /films - пустое тело -> 500")
    void putShouldReturn500WhenEmptyBody() throws IOException, InterruptedException {
        HttpResponse<String> response = sendPut("");
        assertEquals(500, response.statusCode());
    }

    @Test
    @DisplayName("DELETE /films/{id} - существующий фильм -> 200, затем 404")
    void deleteShouldRemoveFilmWhenIdExists() throws Exception {
        Film film = createValidFilm();
        postValidFilm(film);

        HttpResponse<String> deleteResponse = sendDelete(film.getId());

        assertEquals(200, deleteResponse.statusCode());

        HttpResponse<String> getResponse = sendGet(film.getId());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    @DisplayName("DELETE /films/{id} - несуществующий фильм -> 404")
    void deleteShouldReturn404WhenFilmNotExists() throws Exception {
        HttpResponse<String> response = sendDelete(9999L);

        assertEquals(404, response.statusCode());
    }

    private Film createValidFilm() {
        FilmRating mpa = new FilmRating();
        mpa.setId(2L);
        mpa.setName("PG");

        Film film = new Film();
        film.setId(1L);
        film.setName("test name");
        film.setDescription("test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpa(mpa);
        return film;
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

    private HttpResponse<String> sendDelete(long filmId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + filmId))
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGet(long filmId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + filmId))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void postValidFilm(Film film) throws IOException, InterruptedException {
        HttpResponse<String> postResponse = sendPost(gson.toJson(film));
        assertEquals(200, postResponse.statusCode());
        Film createdFilm = gson.fromJson(postResponse.body(), Film.class);
        film.setId(createdFilm.getId());
    }
}
