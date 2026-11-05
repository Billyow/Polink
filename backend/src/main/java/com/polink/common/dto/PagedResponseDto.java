package com.polink.common.dto;

import com.polink.common.exception.DomainException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

@Schema(description = "Respuesta paginada estándar")
public record PagedResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static PageRequest pageRequest(int page, int size) {
        if (page < 0) {
            throw new DomainException("El número de página no puede ser negativo", HttpStatus.BAD_REQUEST);
        }
        if (size < 1 || size > 100) {
            throw new DomainException("El tamaño de página debe estar entre 1 y 100", HttpStatus.BAD_REQUEST);
        }
        return PageRequest.of(page, size);
    }

    public static <T> PagedResponseDto<T> from(Page<T> pageData) {
        return new PagedResponseDto<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
