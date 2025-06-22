package com.backend.biblioteca.controller;

import com.backend.biblioteca.dto.request.LibroCreateRequest;
import com.backend.biblioteca.dto.response.PaginationResponse;
import com.backend.biblioteca.model.Libro;
import com.backend.biblioteca.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/libros")
@CrossOrigin(origins = "*")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @GetMapping
    public ResponseEntity<PaginationResponse<Libro>> getLibros(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            // Filtros de búsqueda (opcionales)
            @RequestParam(required = false) String codigoLibro,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String editorial,
            @RequestParam(required = false) Integer anioPublicacion,
            @RequestParam(required = false) Integer cantidadDisponible,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) String search) {

        PaginationResponse<Libro> response = libroService.searchLibros(
                page, size, sortBy, sortDirection,
                codigoLibro, titulo, autor, editorial,
                anioPublicacion, cantidadDisponible, disponible, search
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> getLibroById(@PathVariable Long id) {
        Libro libro = libroService.getLibroById(id);
        return ResponseEntity.ok(libro);
    }

    @PostMapping
    public ResponseEntity<Libro> createLibro(@Valid @RequestBody LibroCreateRequest request) {
        Libro nuevoLibro = libroService.createLibro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoLibro);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> updateLibro(@PathVariable Long id,
                                             @Valid @RequestBody LibroCreateRequest request) {
        Libro libroActualizado = libroService.updateLibro(id, request);
        return ResponseEntity.ok(libroActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLibro(@PathVariable Long id) {
        libroService.deleteLibro(id);
        return ResponseEntity.noContent().build();
    }
}