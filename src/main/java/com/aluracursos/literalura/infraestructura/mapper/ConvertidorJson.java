package com.aluracursos.literalura.infraestructura.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class ConvertidorJson {

    private final ObjectMapper mapper = new ObjectMapper();

    public static record AutorDTO(String nombre, Integer anioNacimiento, Integer anioFallecimiento) {}
    public static record LibroDTO(String titulo, String idioma, Integer descargas, List<AutorDTO> autores) {}

    public List<LibroDTO> parsearLibrosDesdeGutendex(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode resultados = root.get("results");
            List<LibroDTO> libros = new ArrayList<>();
            if (resultados != null && resultados.isArray()) {
                for (JsonNode n : resultados) {
                    String titulo = n.path("title").asText();
                    String idioma = n.path("languages").isArray() && n.path("languages").size() > 0
                            ? n.path("languages").get(0).asText() : null;
                    Integer descargas = n.path("download_count").isInt() ? n.path("download_count").asInt() : null;

                    List<AutorDTO> autores = new ArrayList<>();
                    JsonNode autoresNode = n.path("authors");
                    if (autoresNode.isArray()) {
                        for (JsonNode a : autoresNode) {
                            String nombre = a.path("name").asText(null);
                            Integer nacimiento = a.path("birth_year").isInt() ? a.path("birth_year").asInt() : null;
                            Integer fallecimiento = a.path("death_year").isInt() ? a.path("death_year").asInt() : null;
                            autores.add(new AutorDTO(nombre, nacimiento, fallecimiento));
                        }
                    }
                    libros.add(new LibroDTO(titulo, idioma, descargas, autores));
                }
            }
            return libros;
        } catch (Exception e) {
            throw new RuntimeException("No fue posible convertir el JSON: " + e.getMessage(), e);
        }
    }
}