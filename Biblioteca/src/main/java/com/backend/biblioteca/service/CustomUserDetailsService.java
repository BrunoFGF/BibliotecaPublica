package com.backend.biblioteca.service;

import com.backend.biblioteca.model.Usuario;
import com.backend.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== DEBUG LOGIN START ===");
        System.out.println("1. Buscando usuario con email: " + email);

        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

            System.out.println("2. Usuario encontrado:");
            System.out.println("   - ID: " + usuario.getId());
            System.out.println("   - Nombre: " + usuario.getNombre());
            System.out.println("   - Email: " + usuario.getEmail());
            System.out.println("   - Activo: " + usuario.getActivo());
            System.out.println("   - Email verificado: " + usuario.getEmailVerificado());

            // Verificar roles de forma segura
            System.out.println("3. Verificando roles...");
            if (usuario.getRoles() == null) {
                System.out.println("   - ERROR: Roles es null");
                throw new UsernameNotFoundException("Usuario sin roles asignados: " + email);
            }

            System.out.println("   - Cantidad de roles: " + usuario.getRoles().size());
            usuario.getRoles().forEach(rol -> {
                System.out.println("   - Rol: " + rol.getNombre() + " (ID: " + rol.getId() + ")");
            });

            // Verificar si el usuario está activo
            System.out.println("4. Verificando estado activo...");
            if (!usuario.getActivo()) {
                System.out.println("   - ERROR: Usuario inactivo");
                throw new UsernameNotFoundException("Usuario inactivo: " + email);
            }

            // Verificar si es bibliotecario
            System.out.println("5. Verificando rol bibliotecario...");
            boolean esBibliotecario = usuario.getRoles().stream()
                    .anyMatch(rol -> "BIBLIOTECARIO".equals(rol.getNombre()));
            System.out.println("   - Es bibliotecario: " + esBibliotecario);

            if (!esBibliotecario) {
                System.out.println("   - ERROR: No es bibliotecario");
                throw new UsernameNotFoundException("Acceso denegado. Solo bibliotecarios pueden iniciar sesión: " + email);
            }

            System.out.println("6. Verificando authorities...");
            try {
                var authorities = usuario.getAuthorities();
                System.out.println("   - Authorities: " + authorities.stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                System.out.println("   - ERROR generando authorities: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

            System.out.println("7. Login exitoso para: " + email);
            System.out.println("=== DEBUG LOGIN END ===");

            return usuario;

        } catch (Exception e) {
            System.out.println("=== ERROR EN LOGIN ===");
            System.out.println("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== END ERROR ===");
            throw e;
        }
    }

    // Método para obtener usuario por email (solo bibliotecarios)
    public Usuario findByEmail(String email) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

            if (!usuario.esBibliotecario()) {
                throw new UsernameNotFoundException("Acceso denegado: " + email);
            }

            return usuario;
        } catch (Exception e) {
            System.out.println("Error en findByEmail: " + e.getMessage());
            throw e;
        }
    }
}