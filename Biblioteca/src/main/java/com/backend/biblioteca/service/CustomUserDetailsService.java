package com.backend.biblioteca.service;

import com.backend.biblioteca.model.Usuario;
import com.backend.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Verificar si el usuario está activo
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        // IMPORTANTE: Solo permitir login a bibliotecarios
        if (!usuario.esBibliotecario()) {
            throw new UsernameNotFoundException("Acceso denegado. Solo bibliotecarios pueden iniciar sesión: " + email);
        }

        return usuario;
    }

    // Método para obtener usuario por email (solo bibliotecarios)
    public Usuario findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (!usuario.esBibliotecario()) {
            throw new UsernameNotFoundException("Acceso denegado: " + email);
        }

        return usuario;
    }
}