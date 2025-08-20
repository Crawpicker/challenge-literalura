package com.aluracursos.literalura.aplicacion;

import com.aluracursos.literalura.dominio.Autor;
import com.aluracursos.literalura.dominio.Libro;
import com.aluracursos.literalura.infraestructura.cliente.ClienteGutendex;
import com.aluracursos.literalura.infraestructura.mapper.ConvertidorJson;
import com.aluracursos.literalura.infraestructura.mapper.ConvertidorJson.LibroDTO;
import com.aluracursos.literalura.infraestructura.mapper.ConvertidorJson.AutorDTO;
import com.aluracursos.literalura.repositorio.AutorRepositorio;
import com.aluracursos.literalura.repositorio.LibroRepositorio;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class Consola {

    private final AutorRepositorio autores;
    private final LibroRepositorio libros;
    private final ClienteGutendex cliente;
    private final ConvertidorJson convertidor;

    public Consola(AutorRepositorio autores, LibroRepositorio libros) {
        this.autores = autores;
        this.libros = libros;
        this.cliente = new ClienteGutendex();
        this.convertidor = new ConvertidorJson();
    }

    public void mostrarMenuInteractivo() {
        var entrada = new Scanner(System.in);
        var opcion = -1;
        while (opcion != 0) {
            System.out.println(
                    "\n============ LiterAlura ============" +
                    "\n1) Buscar libro por título (Gutendex)" +
                    "\n2) Listar libros" +
                    "\n3) Listar autores" +
                    "\n4) Listar autores vivos en un año" +
                    "\n5) Listar libros por idioma (es, en, pt, fr, ...)" +
                    "\n0) Salir\n"
            );
            System.out.print("Elige una opción: ");
            try {
                opcion = Integer.parseInt(entrada.nextLine().trim());
            } catch (NumberFormatException ex) {
                opcion = -1;
            }
            switch (opcion) {
                case 1 -> buscarYGuardarLibro(entrada);
                case 2 -> listarLibros();
                case 3 -> listarAutores();
                case 4 -> listarAutoresVivos(entrada);
                case 5 -> listarLibrosPorIdioma(entrada);
                case 0 -> System.out.println("¡Hasta luego!");
                default -> System.out.println("Opción no válida.");
            }
        }
    }

    private void buscarYGuardarLibro(Scanner entrada) {
        System.out.print("Título a buscar: ");
        var titulo = entrada.nextLine().trim();
        if (titulo.isEmpty()) {
            System.out.println("El título no puede estar vacío.");
            return;
        }
        try {
            String json = cliente.buscarPorTitulo(titulo);
            List<LibroDTO> resultados = convertidor.parsearLibrosDesdeGutendex(json);
            if (resultados.isEmpty()) {
                System.out.println("No se encontraron resultados.");
                return;
            }
            // Tomamos el primer resultado más relevante
            LibroDTO dto = resultados.get(0);

            if (dto.titulo() == null || dto.titulo().isBlank()) {
                System.out.println("Resultado inválido sin título.");
                return;
            }
            if (libros.existsByTituloIgnoreCase(dto.titulo())) {
                System.out.println("El libro ya existe en la base de datos.");
                return;
            }

            Libro libro = new Libro(dto.titulo(), dto.idioma(), dto.descargas());
            // Asignar autor (tomar el primero si hay varios)
            Autor autor = null;
            if (dto.autores() != null && !dto.autores().isEmpty()) {
                AutorDTO a = dto.autores().get(0);
                String nombreAutor = a.nombre();
                if (nombreAutor != null) {
                    autor = Optional.ofNullable(autores.findByNombreIgnoreCase(nombreAutor))
                            .orElseGet(() -> autores.save(new Autor(nombreAutor, a.anioNacimiento(), a.anioFallecimiento())));
                }
            }
            if (autor != null) {
                autor.agregarLibro(libro);
                autores.save(autor); // cascade persist libro
            } else {
                libros.save(libro);
            }

            System.out.println("Guardado: " + libro.getTitulo() + (autor != null ? " — " + autor.getNombre() : ""));

        } catch (Exception e) {
            System.out.println("Error al procesar la búsqueda: " + e.getMessage());
        }
    }

    private void listarLibros() {
        var lista = libros.findAll();
        if (lista.isEmpty()) {
            System.out.println("No hay libros guardados.");
            return;
        }
        lista.stream()
                .sorted(Comparator.comparing(Libro::getTitulo, String.CASE_INSENSITIVE_ORDER))
                .forEach(l -> System.out.printf("• %s%s  [idioma: %s, descargas: %s]%n",
                        l.getTitulo(),
                        l.getAutor() != null ? " — " + l.getAutor().getNombre() : "",
                        l.getIdioma(),
                        l.getDescargas() == null ? "?" : l.getDescargas().toString()));
    }

    private void listarAutores() {
        var lista = autores.findAll();
        if (lista.isEmpty()) {
            System.out.println("No hay autores guardados.");
            return;
        }
        lista.stream()
                .sorted(Comparator.comparing(Autor::getNombre, String.CASE_INSENSITIVE_ORDER))
                .forEach(a -> System.out.printf("• %s (nac: %s, def: %s) — libros: %d%n",
                        a.getNombre(),
                        a.getAnioNacimiento(),
                        a.getAnioFallecimiento(),
                        a.getLibros().size()));
    }

    private void listarAutoresVivos(Scanner entrada) {
        System.out.print("Año: ");
        try {
            int anio = Integer.parseInt(entrada.nextLine().trim());
            var lista = autores.autoresVivosEn(anio);
            if (lista.isEmpty()) {
                System.out.println("No se encontraron autores vivos en " + anio);
                return;
            }
            lista.forEach(a -> System.out.printf("• %s (nac: %s, def: %s)%n",
                    a.getNombre(), a.getAnioNacimiento(), a.getAnioFallecimiento()));
        } catch (NumberFormatException ex) {
            System.out.println("Ingresa un año válido.");
        }
    }

    private void listarLibrosPorIdioma(Scanner entrada) {
        System.out.print("Código de idioma (es, en, pt, fr, ...): ");
        var idioma = entrada.nextLine().trim();
        var lista = libros.findByIdiomaIgnoreCase(idioma);
        if (lista.isEmpty()) {
            System.out.println("No hay libros en ese idioma.");
            return;
        }
        lista.forEach(l -> System.out.printf("• %s — %s%n",
                l.getTitulo(),
                l.getAutor() != null ? l.getAutor().getNombre() : "Autor desconocido"));
    }
}