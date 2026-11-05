package com.polink.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDomainExceptionsShouldReturnExpectedHttpStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/notifications/123");

        ResponseEntity<ApiErrorResponse> notFound = handler.handleDomainException(
                new ResourceNotFoundException("No encontrado"), request);
        ResponseEntity<ApiErrorResponse> conflict = handler.handleDomainException(
                new ConflictException("Conflicto de horario"), request);

        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notFound.getBody()).isNotNull();
        assertThat(notFound.getBody().message()).isEqualTo("No encontrado");
        assertThat(notFound.getBody().path()).isEqualTo("/api/v1/notifications/123");

        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conflict.getBody()).isNotNull();
        assertThat(conflict.getBody().message()).isEqualTo("Conflicto de horario");
    }

    @Test
    void handleMaxUploadAndAccessDeniedAndGenericShouldReturnStructuredError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/tutor/offerings");

        ResponseEntity<ApiErrorResponse> tooLarge = handler.handleMaxUploadSize(
                new MaxUploadSizeExceededException(5 * 1024 * 1024), request);
        ResponseEntity<ApiErrorResponse> forbidden = handler.handleAccessDenied(
                new AccessDeniedException("Denied"), request);
        ResponseEntity<ApiErrorResponse> internal = handler.handleGenericException(
                new RuntimeException("Unexpected"), request);

        assertThat(tooLarge.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(internal.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
