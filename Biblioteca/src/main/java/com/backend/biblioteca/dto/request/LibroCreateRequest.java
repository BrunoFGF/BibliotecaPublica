package com.backend.biblioteca.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibroCreateRequest {

    @NotBlank(message = "El código del libro es obligatorio")
    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El código solo puede contener letras y números")
    private String codigoLibro;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 60, message = "El autor no puede exceder 60 caracteres")
    private String autor;

    @NotBlank(message = "La editorial es obligatoria")
    @Size(max = 60, message = "La editorial no puede exceder 60 caracteres")
    private String editorial;

    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1000, message = "El año debe ser mayor a 999")
    @Max(value = 9999, message = "El año debe ser menor a 10000")
    private Integer anioPublicacion;

    @NotNull(message = "La cantidad disponible es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidadDisponible;
}