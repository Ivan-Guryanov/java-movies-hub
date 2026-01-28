package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store;

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

    }

    @BeforeEach
    void beforeEach() {
        store.removeAll();
        store.setCounter(0);
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 200, "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_whenRecords_returnsValuesArray() throws Exception {

        store.newFilm("Любовь и голуби", 1985);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 200, "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Любовь и голуби"), "Текст ответа должен содержать название фильма");
        assertTrue(body.contains("1985"), "Текст ответа должен содержать год выпуска");

    }

    @Test
    void postMovies_should_Successfully_AddMovie_ToStore() throws Exception {
        String movieJson = "{\"title\":\"Любовь и голуби\", \"year\":1985}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(movieJson))
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 201, "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("1"), "Текст ответа должен содержать айди");
        assertTrue(body.contains("Любовь и голуби"), "Текст ответа должен содержать название фильма");
        assertTrue(body.contains("1985"), "Текст ответа должен содержать год выпуска");
    }

    @Test
    void postMovies_shouldReturnErrorsForInvalidMovieData() throws Exception {
        String movieJson = "{\"title\":\"\", \"year\":1887}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(movieJson))
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 422, "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Поле с названием фильма не заполнено."));
        assertTrue(body.contains("Год должен быть больше или равен 1888."));
        assertTrue(body.contains("Ошибка валидации"));
    }

    @Test
    void postMovies_shouldReturnErrorsForInvalidMovieData2() throws Exception {
        String movieJson = "{\"title\":\"Спецификация даёт нам общее представление о том, как должен работать сервис: " +
                "какие есть эндпоинты, какие коды ответа возвращать, какие ошибки обрабатывать. Но для работы по TDD " +
                "этого недостаточно — нужны конкретные проверки, которые можно превратить в тесты. По сути, это перевод" +
                "с язык аналитика» на «язык разработчика»: вместо общих правил формулируем простые и понятные сценарии —" +
                " что отправляем на сервер и какой результат должны получить.\", " +
                "\"year\":2028}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(movieJson))
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 422, "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Название не должно превышать 100 символов."));
        assertTrue(body.contains("Год должен быть меньше или равен " + (Year.now().getValue() + 1) + "."));
        assertTrue(body.contains("Ошибка валидации"));
    }

    @Test
    void getMoviesId_shouldReturnMovieByCorrectId() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/1"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 200, "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Любовь и голуби"), "Текст ответа должен содержать название фильма");
        assertTrue(body.contains("1985"), "Текст ответа должен содержать год выпуска");

    }

    @Test
    void getMoviesId_shouldReturnMovieNoCorrectId() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/2"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 200, "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertFalse(body.contains("Любовь и голуби"), "Текст ответа должен содержать название фильма");
        assertFalse(body.contains("1985"), "Текст ответа должен содержать год выпуска");

    }

    @Test
    void getMoviesId_shouldReturnMovieNoCorrectId2() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/3"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 404, "GET /movies должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Фильм не найден"), "Текст ответа должен содержать описание ошибки");

    }

    @Test
    void getMoviesId_shouldReturnMovieNoCorrectId3() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/а"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 400, "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Не корректный ID"), "Текст ответа должен содержать описание ошибки");
    }

    @Test
    void deleteMoviesId_DeleteMovieByID() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/1"))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(500))
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 204, "DELETE /movies/1 должен вернуть 204");

        HttpRequest req1 = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler1 =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp1 = client.send(req1, responseBodyHandler1);

        assertEquals(resp1.statusCode(), 200, "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp1.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp1.body().trim();
        assertFalse(body.contains("Любовь и голуби"), "Текст ответа должен содержать название фильма");
        assertFalse(body.contains("1985"), "Текст ответа должен содержать год выпуска");

    }

    @Test
    void deleteMoviesId_DeleteMovieByNoCorrectID() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/3"))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(500))
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 404, "GET /movies должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Фильм не найден"), "Текст ответа должен содержать описание ошибки");
    }

    @Test
    void deleteMoviesId_DeleteMovieByNoCorrectID2() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/a"))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(500))
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 400, "GET /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Не корректный ID"), "Текст ответа должен содержать описание ошибки");
    }

    @Test
    void getMoviesYear_shouldReturnMovieByCorrectYear() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=1985"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 200, "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Любовь и голуби"), "Текст ответа должен содержать название фильма");
        assertTrue(body.contains("1985"), "Текст ответа должен содержать год выпуска");

    }

    @Test
    void getMoviesYear_shouldReturnMovieByNowCorrectYear() throws Exception {

        store.newFilm("Любовь и голуби", 1985);
        store.newFilm("Белое солнце пустыни", 1970);

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=a"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        assertEquals(resp.statusCode(), 400, "GET /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.contains("Некорректный параметр запроса — 'year'"), "Текст ответа должен содержать описание ошибки");

    }
}