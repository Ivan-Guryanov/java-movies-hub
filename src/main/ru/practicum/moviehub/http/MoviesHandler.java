package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.store.MoviesStore;
import com.google.gson.Gson;

import java.io.IOException;

public class MoviesHandler extends BaseHttpHandler {
    private final Gson gson = new Gson();

    public MoviesHandler(MoviesStore moviesStore) {

        super(moviesStore);
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
                    sendJson(ex);
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
}
