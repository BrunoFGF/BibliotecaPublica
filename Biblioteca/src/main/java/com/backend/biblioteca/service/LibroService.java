package com.backend.biblioteca.service;

import com.backend.biblioteca.dto.request.LibroCreateRequest;
import com.backend.biblioteca.dto.response.PaginationResponse;
import com.backend.biblioteca.model.Libro;
import com.backend.biblioteca.repository.LibroRepository;
import com.backend.biblioteca.exception.ResourceNotFoundException;
import com.backend.biblioteca.exception.DuplicateResourceException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    @Transactional(readOnly = true)
    public PaginationResponse<Libro> searchLibros(
            int page, int size, String sortBy, String sortDirection,
            String codigoLibro, String titulo, String autor, String editorial,
            Integer anioPublicacion, Integer cantidadDisponible, Boolean disponible,
            String search) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Libro> spec = createDynamicSpecification(
                codigoLibro, titulo, autor, editorial,
                anioPublicacion, cantidadDisponible, disponible, search
        );

        Page<Libro> result = libroRepository.findAll(spec, pageable);
        return PaginationResponse.of(result);
    }

    private Specification<Libro> createDynamicSpecification(
            String codigoLibro, String titulo, String autor, String editorial,
            Integer anioPublicacion, Integer cantidadDisponible, Boolean disponible,
            String search) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (codigoLibro != null && !codigoLibro.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("codigoLibro")),
                        codigoLibro.toUpperCase()
                ));
            }

            if (titulo != null && !titulo.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("titulo")),
                        "%" + titulo.toUpperCase() + "%"
                ));
            }

            if (autor != null && !autor.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("autor")),
                        "%" + autor.toUpperCase() + "%"
                ));
            }

            if (editorial != null && !editorial.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("editorial")),
                        "%" + editorial.toUpperCase() + "%"
                ));
            }

            if (anioPublicacion != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("anioPublicacion"), anioPublicacion
                ));
            }

            if (cantidadDisponible != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("cantidadDisponible"), cantidadDisponible
                ));
            }

            if (disponible != null && disponible) {
                predicates.add(criteriaBuilder.greaterThan(
                        root.get("cantidadDisponible"), 0
                ));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toUpperCase() + "%";

                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("titulo")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("autor")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("codigoLibro")), searchTerm),
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("editorial")), searchTerm)
                );

                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public Libro getLibroById(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "ID", id));
    }

    public Libro createLibro(LibroCreateRequest request) {
        if (libroRepository.existsByCodigoLibroIgnoreCase(request.getCodigoLibro())) {
            throw new DuplicateResourceException("Libro", "código", request.getCodigoLibro());
        }

        Libro libro = new Libro();
        return getLibro(request, libro);
    }

    public Libro updateLibro(Long id, LibroCreateRequest request) {
        Libro libro = getLibroById(id);

        if (libroRepository.existsByCodigoLibroIgnoreCaseAndIdNot(request.getCodigoLibro(), id)) {
            throw new DuplicateResourceException("Libro", "código", request.getCodigoLibro());
        }

        return getLibro(request, libro);
    }

    private Libro getLibro(LibroCreateRequest request, Libro libro) {
        libro.setCodigoLibro(request.getCodigoLibro().toUpperCase());
        libro.setTitulo(request.getTitulo());
        libro.setAutor(request.getAutor());
        libro.setEditorial(request.getEditorial());
        libro.setAnioPublicacion(request.getAnioPublicacion());
        libro.setCantidadDisponible(request.getCantidadDisponible());

        return libroRepository.save(libro);
    }

    public void deleteLibro(Long id) {
        if (!libroRepository.existsById(id)) {
            throw new ResourceNotFoundException("Libro", "ID", id);
        }
        libroRepository.deleteById(id);
    }
}