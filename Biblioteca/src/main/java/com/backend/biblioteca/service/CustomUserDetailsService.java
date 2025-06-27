package com.backend.biblioteca.service;

import com.backend.biblioteca.model.Usuario;
import com.backend.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

            if (!usuario.getActivo()) {
                throw new UsernameNotFoundException("Usuario inactivo: " + email);
            }

            if (!usuario.esBibliotecario()) {
                throw new UsernameNotFoundException("Acceso denegado. Solo bibliotecarios pueden iniciar sesión: " + email);
            }

            return usuario;

        } catch (Exception e) {
            throw new UsernameNotFoundException("Error al cargar usuario: " + e.getMessage());
        }
    }

}