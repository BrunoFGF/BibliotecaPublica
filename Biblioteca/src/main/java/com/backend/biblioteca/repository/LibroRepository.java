package com.backend.biblioteca.repository;

import com.backend.biblioteca.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long>, JpaSpecificationExecutor<Libro> {

    // Verificar si existe un código (para validaciones)
    @Query("SELECT COUNT(l) > 0 FROM Libro l WHERE UPPER(l.codigoLibro) = UPPER(:codigo)")
    boolean existsByCodigoLibroIgnoreCase(@Param("codigo") String codigo);

    // Verificar si existe un código excluyendo un ID específico (para actualizaciones)
    @Query("SELECT COUNT(l) > 0 FROM Libro l WHERE UPPER(l.codigoLibro) = UPPER(:codigo) AND l.id != :id")
    boolean existsByCodigoLibroIgnoreCaseAndIdNot(@Param("codigo") String codigo, @Param("id") Long id);
}