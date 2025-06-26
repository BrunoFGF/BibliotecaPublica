package com.backend.biblioteca.dto.response;

import com.backend.biblioteca.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;

    public static RolResponse fromEntity(Rol rol) {
        RolResponse response = new RolResponse();
        response.setId(rol.getId());
        response.setNombre(rol.getNombre());
        response.setDescripcion(rol.getDescripcion());
        response.setFechaCreacion(rol.getFechaCreacion());
        return response;
    }
}