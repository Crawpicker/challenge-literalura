package com.aluracursos.literalura.repositorio;

import com.aluracursos.literalura.dominio.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutorRepositorio extends JpaRepository<Autor, Long> {

    @Query("SELECT a FROM Autor a WHERE (a.anioNacimiento IS NULL OR a.anioNacimiento <= :anio) " +
            "AND (a.anioFallecimiento IS NULL OR a.anioFallecimiento >= :anio)")
    List<Autor> autoresVivosEn(Integer anio);

    boolean existsByNombreIgnoreCase(String nombre);
    Autor findByNombreIgnoreCase(String nombre);
}