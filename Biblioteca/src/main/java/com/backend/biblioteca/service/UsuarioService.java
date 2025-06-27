package com.backend.biblioteca.service;

import com.backend.biblioteca.dto.request.ChangePasswordRequest;
import com.backend.biblioteca.dto.request.LoginRequest;
import com.backend.biblioteca.dto.request.UsuarioCreateRequest;
import com.backend.biblioteca.dto.request.UsuarioUpdateRequest;
import com.backend.biblioteca.dto.response.LoginResponse;
import com.backend.biblioteca.dto.response.PaginationResponse;
import com.backend.biblioteca.dto.response.UsuarioResponse;
import com.backend.biblioteca.exception.DuplicateResourceException;
import com.backend.biblioteca.exception.ResourceNotFoundException;
import com.backend.biblioteca.model.Rol;
import com.backend.biblioteca.model.Usuario;
import com.backend.biblioteca.repository.RolRepository;
import com.backend.biblioteca.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    public LoginResponse login(LoginRequest request) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

            if (!request.getPassword().equals(usuario.getPassword())) {
                throw new BadCredentialsException("Contraseña incorrecta");
            }

            if (!usuario.esBibliotecario()) {
                throw new BadCredentialsException("Solo bibliotecarios pueden acceder");
            }

            if (!usuario.getActivo()) {
                throw new BadCredentialsException("Usuario inactivo");
            }

            Set<String> roles = usuario.getRoles().stream()
                    .map(Rol::getNombre)
                    .collect(Collectors.toSet());

            return new LoginResponse(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    roles,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            throw new BadCredentialsException("Error en el login: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaginationResponse<UsuarioResponse> searchUsuarios(
            int page, int size, String sortBy, String sortDirection,
            String nombre, String cedula, String email, String rol,
            Boolean activo, String search) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Usuario> spec = createUsuarioSpecification(
                nombre, cedula, email, rol, activo, search
        );

        Page<Usuario> result = usuarioRepository.findAll(spec, pageable);

        Page<UsuarioResponse> responsePage = result.map(UsuarioResponse::fromEntity);
        return PaginationResponse.of(responsePage);
    }

    private Specification<Usuario> createUsuarioSpecification(
            String nombre, String cedula, String email, String rol,
            Boolean activo, String search) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (nombre != null && !nombre.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("nombre")),
                        "%" + nombre.toUpperCase() + "%"
                ));
            }

            if (cedula != null && !cedula.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("cedula")),
                        "%" + cedula.toUpperCase() + "%"
                ));
            }

            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("email")),
                        "%" + email.toUpperCase() + "%"
                ));
            }

            if (rol != null && !rol.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.join("roles").get("nombre"), rol
                ));
            }

            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toUpperCase() + "%";

                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("nombre")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("email")), searchTerm)
                );

                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "ID", id));
        return UsuarioResponse.fromEntity(usuario);
    }

    public UsuarioResponse createUsuario(UsuarioCreateRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", request.getEmail());
        }

        if (usuarioRepository.existsByCedula(request.getCedula())) {
            throw new DuplicateResourceException("Usuario", "cédula", request.getCedula());
        }

        Usuario usuario = new Usuario();
        usuario.setCedula(request.getCedula());
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail().toLowerCase());
        usuario.setPassword(request.getPassword());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        usuario.setActivo(request.getActivo());
        usuario.setEmailVerificado(true);

        Set<Rol> roles = obtenerRolesPorIds(request.getRolesIds());
        usuario.setRoles(roles);

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(savedUsuario);
    }

    public UsuarioResponse updateUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "ID", id));

        if (usuarioRepository.existsByCedulaAndIdNot(request.getCedula(), id)) {
            throw new DuplicateResourceException("Usuario", "cédula", request.getCedula());
        }

        usuario.setCedula(request.getCedula());
        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        usuario.setActivo(request.getActivo());

        Set<Rol> roles = obtenerRolesPorIds(request.getRolesIds());
        usuario.setRoles(roles);

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(updatedUsuario);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "ID", userId));

        if (!request.getCurrentPassword().equals(usuario.getPassword())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(request.getNewPassword());
        usuarioRepository.save(usuario);
    }

    public void deleteUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "ID", id);
        }

        Usuario usuario = usuarioRepository.findById(id).get();
        if (usuario.esBibliotecario()) {
            long bibliotecarios = usuarioRepository.findByRolNombre("BIBLIOTECARIO").size();
            if (bibliotecarios <= 1) {
                throw new IllegalStateException("No se puede eliminar el último bibliotecario del sistema");
            }
        }

        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Rol> getAllRoles() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        return UsuarioResponse.fromEntity(usuario);
    }

    private Set<Rol> obtenerRolesPorIds(Set<Long> rolesIds) {
        Set<Rol> roles = new HashSet<>();
        for (Long rolId : rolesIds) {
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol", "ID", rolId));
            roles.add(rol);
        }
        return roles;
    }

    @Transactional(readOnly = true)
    public long countUsuariosActivos() {
        return usuarioRepository.countUsuariosActivos();
    }

    @Transactional(readOnly = true)
    public long countUsuariosRegistradosEsteMes() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return usuarioRepository.countUsuariosRegistradosDesde(inicioMes);
    }
}