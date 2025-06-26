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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // AUTENTICACIÓN - Solo permitir login a bibliotecarios
    public LoginResponse login(LoginRequest request) {
        try {
            // Verificar primero que el usuario existe y es bibliotecario
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

            // Verificar que el usuario es bibliotecario antes de intentar autenticar
            if (!usuario.esBibliotecario()) {
                throw new BadCredentialsException("Acceso denegado. Solo bibliotecarios pueden iniciar sesión.");
            }

            // Verificar que está activo
            if (!usuario.getActivo()) {
                throw new BadCredentialsException("Usuario inactivo");
            }

            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Obtener usuario autenticado
            Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();

            // Crear respuesta
            Set<String> roles = usuarioAutenticado.getRoles().stream()
                    .map(Rol::getNombre)
                    .collect(Collectors.toSet());

            return new LoginResponse(
                    usuarioAutenticado.getId(),
                    usuarioAutenticado.getNombre(),
                    usuarioAutenticado.getEmail(),
                    roles,
                    LocalDateTime.now()
            );

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas o acceso denegado");
        }
    }

    // BÚSQUEDA PAGINADA CON FILTROS
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

    // CREAR ESPECIFICACIÓN DINÁMICA PARA FILTROS
    private Specification<Usuario> createUsuarioSpecification(
            String nombre, String apellido, String email, String rol,
            Boolean activo, String search) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro por nombre (parcial)
            if (nombre != null && !nombre.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("nombre")),
                        "%" + nombre.toUpperCase() + "%"
                ));
            }

            // Filtro por apellido (parcial)
            if (apellido != null && !apellido.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("apellido")),
                        "%" + apellido.toUpperCase() + "%"
                ));
            }

            // Filtro por email (parcial)
            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("email")),
                        "%" + email.toUpperCase() + "%"
                ));
            }

            // Filtro por rol
            if (rol != null && !rol.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.join("roles").get("nombre"), rol
                ));
            }

            // Filtro por estado activo
            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            // Búsqueda general (nombre, apellido, email)
            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toUpperCase() + "%";

                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("nombre")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("apellido")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("email")), searchTerm)
                );

                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // OBTENER USUARIO POR ID
    @Transactional(readOnly = true)
    public UsuarioResponse getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "ID", id));
        return UsuarioResponse.fromEntity(usuario);
    }

    // CREAR USUARIO
    public UsuarioResponse createUsuario(UsuarioCreateRequest request) {
        // Verificar email único
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", request.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail().toLowerCase());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        usuario.setActivo(request.getActivo());
        usuario.setEmailVerificado(true); // Por defecto verificado para usuarios creados por bibliotecario

        // Asignar roles
        Set<Rol> roles = obtenerRolesPorIds(request.getRolesIds());
        usuario.setRoles(roles);

        Usuario savedUsuario = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(savedUsuario);
    }

    // ACTUALIZAR USUARIO
    public UsuarioResponse updateUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "ID", id));

        usuario.setNombre(request.getNombre());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        usuario.setActivo(request.getActivo());

        // Actualizar roles
        Set<Rol> roles = obtenerRolesPorIds(request.getRolesIds());
        usuario.setRoles(roles);

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return UsuarioResponse.fromEntity(updatedUsuario);
    }

    // CAMBIAR CONTRASEÑA
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // Validar que las contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "ID", userId));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    // ELIMINAR USUARIO
    public void deleteUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "ID", id);
        }

        // Verificar que no sea el último bibliotecario
        Usuario usuario = usuarioRepository.findById(id).get();
        if (usuario.esBibliotecario()) {
            long bibliotecarios = usuarioRepository.findByRolNombre("BIBLIOTECARIO").size();
            if (bibliotecarios <= 1) {
                throw new IllegalStateException("No se puede eliminar el último bibliotecario del sistema");
            }
        }

        usuarioRepository.deleteById(id);
    }

    // OBTENER TODOS LOS ROLES
    @Transactional(readOnly = true)
    public List<Rol> getAllRoles() {
        return rolRepository.findAll();
    }

    // OBTENER USUARIO ACTUAL (AUTENTICADO)
    @Transactional(readOnly = true)
    public UsuarioResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));

        return UsuarioResponse.fromEntity(usuario);
    }

    // MÉTODOS AUXILIARES
    private Set<Rol> obtenerRolesPorIds(Set<Long> rolesIds) {
        Set<Rol> roles = new HashSet<>();
        for (Long rolId : rolesIds) {
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol", "ID", rolId));
            roles.add(rol);
        }
        return roles;
    }

    // VERIFICAR SI EL USUARIO ACTUAL ES BIBLIOTECARIO
    @Transactional(readOnly = true)
    public boolean isCurrentUserBibliotecario() {
        try {
            UsuarioResponse currentUser = getCurrentUser();
            return currentUser.getRoles().stream()
                    .anyMatch(rol -> "BIBLIOTECARIO".equals(rol.getNombre()));
        } catch (Exception e) {
            return false;
        }
    }

    // ESTADÍSTICAS
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