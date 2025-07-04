package com.backend.biblioteca.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cedula", nullable = false, length = 10)
    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 10, max = 10, message = "La cédula debe tener exactamente 10 dígitos")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe contener solo números")
    private String cedula;

    @Column(name = "nombre", nullable = false, length = 30)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre no puede exceder 30 caracteres")
    private String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 60)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 60, message = "El email no puede exceder 60 caracteres")
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @Column(name = "telefono", length = 10)
    @Size(max = 10, message = "El teléfono no puede exceder 10 caracteres")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe contener exactamente 10 dígitos")
    private String telefono;

    @Column(name = "direccion", length = 60)
    @Size(max = 60, message = "La dirección no puede exceder 60 caracteres")
    private String direccion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "email_verificado", nullable = false)
    private Boolean emailVerificado = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .filter(rol -> rol != null && rol.getNombre() != null)
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return activo != null ? activo : false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return (activo != null ? activo : false) && (emailVerificado != null ? emailVerificado : false);
    }

    public boolean tieneRol(String nombreRol) {
        if (roles == null || nombreRol == null) {
            return false;
        }
        return roles.stream()
                .filter(rol -> rol != null)
                .anyMatch(rol -> nombreRol.equals(rol.getNombre()));
    }

    public boolean esBibliotecario() {
        return tieneRol("BIBLIOTECARIO");
    }

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