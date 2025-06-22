package com.backend.biblioteca.controller;

import com.backend.biblioteca.dto.request.PaginationRequest;
import com.backend.biblioteca.dto.response.PaginationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;

public abstract class BaseController<T> {
    @GetMapping
    public ResponseEntity<PaginationResponse<T>> getAll(@Valid PaginationRequest request) {
        PaginationResponse<T> response = getPaginatedResults(request);
        return ResponseEntity.ok(response);
    }

    // Método alternativo con parámetros individuales
    @GetMapping("/list")
    public ResponseEntity<PaginationResponse<T>> getAllWithParams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(defaultValue = "") String search) {

        PaginationRequest request = new PaginationRequest(
                page, size, sortBy, sortDirection, search
        );

        PaginationResponse<T> response = getPaginatedResults(request);
        return ResponseEntity.ok(response);
    }

    protected abstract PaginationResponse<T> getPaginatedResults(PaginationRequest request);
}