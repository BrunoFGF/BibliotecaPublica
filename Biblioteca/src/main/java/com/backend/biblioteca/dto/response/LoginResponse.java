package com.backend.biblioteca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String nombre;
    private String email;
    private Set<String> roles;
    private LocalDateTime loginTime;

    public String getNombreCompleto() {
        return nombre;
    }
}