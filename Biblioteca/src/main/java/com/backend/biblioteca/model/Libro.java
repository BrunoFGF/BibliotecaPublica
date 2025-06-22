package com.backend.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "libros")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_libro", nullable = false, unique = true, length = 20)
    @NotBlank(message = "El código del libro es obligatorio")
    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El código solo puede contener letras y números")
    private String codigoLibro;

    @Column(name = "titulo", nullable = false, length = 100)
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    private String titulo;

    @Column(name = "autor", nullable = false, length = 60)
    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 60, message = "El autor no puede exceder 60 caracteres")
    private String autor;

    @Column(name = "editorial", nullable = false, length = 60)
    @NotBlank(message = "La editorial es obligatoria")
    @Size(max = 60, message = "La editorial no puede exceder 60 caracteres")
    private String editorial;

    @Column(name = "anio_publicacion", nullable = false)
    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1000, message = "El año debe ser mayor a 999")
    @Max(value = 9999, message = "El año debe ser menor a 10000")
    private Integer anioPublicacion;

    @Column(name = "cantidad_disponible", nullable = false)
    @NotNull(message = "La cantidad disponible es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidadDisponible;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}