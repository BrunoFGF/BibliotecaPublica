package com.backend.biblioteca.dto.response;

import com.backend.biblioteca.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String cedula;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private Boolean activo;
    private Boolean emailVerificado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Set<RolResponse> roles;

    public String getNombreCompleto() {
        return nombre;
    }

    public static UsuarioResponse fromEntity(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setCedula(usuario.getCedula());
        response.setNombre(usuario.getNombre());
        response.setEmail(usuario.getEmail());
        response.setTelefono(usuario.getTelefono());
        response.setDireccion(usuario.getDireccion());
        response.setActivo(usuario.getActivo());
        response.setEmailVerificado(usuario.getEmailVerificado());
        response.setFechaCreacion(usuario.getFechaCreacion());
        response.setFechaActualizacion(usuario.getFechaActualizacion());

        if (usuario.getRoles() != null) {
            response.setRoles(usuario.getRoles().stream()
                    .map(RolResponse::fromEntity)
                    .collect(Collectors.toSet()));
        }

        return response;
    }
}