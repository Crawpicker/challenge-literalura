package com.aluracursos.literalura.infraestructura.cliente;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClienteGutendex {

    private static final String BASE_URL = "https://gutendex.com/books/";

    public String buscarPorTitulo(String titulo) {
        try {
            String query = "?search=" + java.net.URLEncoder.encode(titulo, java.nio.charset.StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + query))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Error consultando Gutendex: " + e.getMessage(), e);
        }
    }
}