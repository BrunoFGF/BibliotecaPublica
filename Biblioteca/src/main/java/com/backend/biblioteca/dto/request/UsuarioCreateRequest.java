package com.backend.biblioteca.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateRequest {

    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 10, max = 10, message = "La cédula debe tener exactamente 10 dígitos")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe contener solo números")
    private String cedula;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre no puede exceder 30 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 60, message = "El email no puede exceder 60 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
    private String password;

    @Size(max = 10, message = "El teléfono no puede exceder 10 caracteres")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    private String telefono;

    @Size(max = 60, message = "La dirección no puede exceder 60 caracteres")
    private String direccion;

    @NotNull(message = "Debe especificar si el usuario está activo")
    private Boolean activo = true;

    @NotEmpty(message = "Debe asignar al menos un rol")
    private Set<Long> rolesIds;
}