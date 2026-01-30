package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;
    private final Gson gson = new Gson();

    public MoviesHandler(MoviesStore moviesStore) {

        this.moviesStore = moviesStore;
    }


    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();
        String query = ex.getRequestURI().getQuery();
        String path = ex.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (method.equalsIgnoreCase("GET")) {
            if (pathParts.length == 2) {
                if (query != null && query.contains("year=")) {
                    sendMovieByYear(ex, query);
                } else {
                    getMovie(ex);
                }
            } else if (pathParts.length == 3) {
                sendMovieById(ex, pathParts[2]);
            }
        } else if (method.equalsIgnoreCase("POST")) {
            setMovie(ex);
        } else if (method.equalsIgnoreCase("DELETE")) {
            deleteMovieById(ex, pathParts[2]);
        } else {
            ex.sendResponseHeaders(405, -1);
            ex.close();
        }
    }

    protected void getMovie(HttpExchange ex) throws IOException {

        List<Movie> list = moviesStore.getAllMovies();
        String jsonResponse = gson.toJson(list);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        sendJson(ex, responseBytes, 200);

    }


    protected void setMovie(HttpExchange ex) throws IOException {

        try {

            byte[] requestBytes = ex.getRequestBody().readAllBytes();
            String body = new String(requestBytes, StandardCharsets.UTF_8);

            String jsonResponse;
            int code;

            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            Headers headers = ex.getRequestHeaders();

            String title = jsonObject.get("title").getAsString();
            int year = jsonObject.get("year").getAsInt();

            int minYear = 1888;
            int maxYear = Year.now().getValue() + 1;

            if (!headers.containsKey("Content-Type")) {
                code = 415;
                jsonResponse = gson.toJson(new ErrorResponse("Некорректный запрос", title, year));
            } else if (!title.isEmpty() && title.length() <= 100 && year >= minYear && year <= maxYear) {
                moviesStore.newFilm(title, year);
                jsonResponse = gson.toJson(moviesStore.getLastMovie());
                code = 201;
            } else {
                jsonResponse = gson.toJson(new ErrorResponse("Ошибка валидации", title, year));
                code = 422;
            }

            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            sendJson(ex, responseBytes, code);

        } catch (Exception e) {
            try {
                ex.sendResponseHeaders(400, -1);
            } catch (IOException ignored) {
            }
        } finally {
            ex.close();
        }
    }

    protected void sendMovieById(HttpExchange ex, String idtxt) throws IOException {

        String jsonResponse;
        int code;

        int id;
        try {
            id = Integer.parseInt(idtxt);
            Movie movie = moviesStore.getListOfFilms().get(id);
            if (movie != null) {
                jsonResponse = gson.toJson(movie);
                code = 200;
            } else {
                jsonResponse = "{\"error\":\"Фильм не найден\"}";
                code = 404;
            }
        } catch (NumberFormatException e) {
            jsonResponse = "{\"error\":\"Не корректный ID\"}";
            ;
            code = 400;
        }

        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        sendJson(ex, responseBytes, code);
    }

    protected void deleteMovieById(HttpExchange ex, String idtxt) throws IOException {

        String jsonResponse;
        int code;

        int id;
        try {
            id = Integer.parseInt(idtxt);
            if (moviesStore.getListOfFilms().containsKey(id)) {
                moviesStore.deleteMovie(id);
                jsonResponse = null;
                code = 204;
            } else {
                jsonResponse = "{\"error\":\"Фильм не найден\"}";
                code = 404;
            }
        } catch (NumberFormatException e) {
            jsonResponse = "{\"error\":\"Не корректный ID\"}";
            ;
            code = 400;
        }

        if (code == 204) {
            ex.sendResponseHeaders(204, -1);
        } else {
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            sendJson(ex, responseBytes, code);
        }
    }

    protected void sendMovieByYear(HttpExchange ex, String yearTxt) throws IOException {

        String jsonResponse;
        int code;

        try {
            String yearString = yearTxt.split("year=")[1].split("&")[0];
            int year = Integer.parseInt(yearString);
            List<Movie> filteredMovies = moviesStore.getListOfFilms().values().stream()
                    .filter(movie -> movie.getYear() == year)
                    .toList();

            jsonResponse = gson.toJson(filteredMovies);
            code = 200;
        } catch (Exception e) {
            jsonResponse = "{\"error\":\"Некорректный параметр запроса — 'year'\"}";
            ;
            code = 400;
        }

        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        sendJson(ex, responseBytes, code);
    }
}
