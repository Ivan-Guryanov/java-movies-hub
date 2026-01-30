package ru.practicum.moviehub.http;


import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;


public abstract class BaseHttpHandler implements HttpHandler {

    protected static final String CT_JSON = "application/json; charset=UTF-8"; // !!! Укажите содержимое заголовка Content-Type

    protected void sendJson(HttpExchange ex, byte[] responseBytes, int cod) throws IOException {

        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(cod, responseBytes.length);
        try (java.io.OutputStream os = ex.getResponseBody()) {
            os.write(responseBytes);
        } finally {
            ex.close();
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {

        ex.sendResponseHeaders(204, -1);
        ex.close();
    }
}