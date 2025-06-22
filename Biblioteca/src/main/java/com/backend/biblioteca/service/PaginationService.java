package com.backend.biblioteca.service;

import com.backend.biblioteca.dto.request.PaginationRequest;
import com.backend.biblioteca.dto.response.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

@Service
public class PaginationService {

    public <T> PaginationResponse<T> paginate(
            JpaRepository<T, ?> repository,
            PaginationRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                request.getSort()
        );

        Page<T> page = repository.findAll(pageable);
        return PaginationResponse.of(page);
    }

    public <T> PaginationResponse<T> paginateWithSpecification(
            JpaSpecificationExecutor<T> repository,
            Specification<T> specification,
            PaginationRequest request) {

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                request.getSort()
        );

        Page<T> page = repository.findAll(specification, pageable);
        return PaginationResponse.of(page);
    }
}