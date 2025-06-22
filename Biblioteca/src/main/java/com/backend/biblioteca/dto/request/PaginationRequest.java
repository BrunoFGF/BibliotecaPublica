package com.backend.biblioteca.dto.request;

import org.springframework.data.domain.Sort;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

    @Min(value = 0, message = "La página debe ser mayor o igual a 0")
    private int page = 0;

    @Min(value = 1, message = "El tamaño debe ser mayor a 0")
    @Max(value = 100, message = "El tamaño máximo es 100")
    private int size = 10;

    private String sortBy = "id";

    @Pattern(regexp = "^(ASC|DESC)$", message = "La dirección debe ser ASC o DESC")
    private String sortDirection = "ASC";

    @Size(max = 100, message = "La búsqueda no puede exceder 100 caracteres")
    private String search = "";

    public Sort getSort() {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, sortBy);
    }
}