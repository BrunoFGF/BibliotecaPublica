package com.backend.biblioteca.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SearchSpecification {

    public static <T> Specification<T> createSearchSpecification(
            String searchTerm,
            String... searchFields) {

        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";

            for (String field : searchFields) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(field).as(String.class)),
                                searchPattern
                        )
                );
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
