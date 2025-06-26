package com.backend.biblioteca.controller;

import com.backend.biblioteca.dto.request.ChangePasswordRequest;
import com.backend.biblioteca.dto.request.UsuarioCreateRequest;
import com.backend.biblioteca.dto.request.UsuarioUpdateRequest;
import com.backend.biblioteca.dto.response.PaginationResponse;
import com.backend.biblioteca.dto.response.UsuarioResponse;
import com.backend.biblioteca.model.Rol;
import com.backend.biblioteca.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('BIBLIOTECARIO')")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<PaginationResponse<UsuarioResponse>> getUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String cedula,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String search) {

        PaginationResponse<UsuarioResponse> response = usuarioService.searchUsuarios(
                page, size, sortBy, sortDirection,
                nombre, cedula, email, rol, activo, search
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> getUsuarioById(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.getUsuarioById(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> createUsuario(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse nuevoUsuario = usuarioService.createUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> updateUsuario(@PathVariable Long id,
                                                         @Valid @RequestBody UsuarioUpdateRequest request) {
        UsuarioResponse usuarioActualizado = usuarioService.updateUsuario(id, request);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        usuarioService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Rol>> getAllRoles() {
        List<Rol> roles = usuarioService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/stats/activos")
    public ResponseEntity<Long> getUsuariosActivos() {
        long count = usuarioService.countUsuariosActivos();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/mes")
    public ResponseEntity<Long> getUsuariosEsteMes() {
        long count = usuarioService.countUsuariosRegistradosEsteMes();
        return ResponseEntity.ok(count);
    }
}