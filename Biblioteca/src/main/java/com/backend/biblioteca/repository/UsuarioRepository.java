package com.backend.biblioteca.repository;

import com.backend.biblioteca.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCedula(String cedula);
    boolean existsByCedulaAndIdNot(String cedula, Long id);

    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = :nombreRol")
    java.util.List<Usuario> findByRolNombre(@Param("nombreRol") String nombreRol);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.activo = true")
    long countUsuariosActivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaCreacion >= :fechaInicio")
    long countUsuariosRegistradosDesde(@Param("fechaInicio") LocalDateTime fechaInicio);
}