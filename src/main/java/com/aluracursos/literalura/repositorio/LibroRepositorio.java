package com.aluracursos.literalura.repositorio;

import com.aluracursos.literalura.dominio.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibroRepositorio extends JpaRepository<Libro, Long> {
    List<Libro> findByIdiomaIgnoreCase(String idioma);
    boolean existsByTituloIgnoreCase(String titulo);
}