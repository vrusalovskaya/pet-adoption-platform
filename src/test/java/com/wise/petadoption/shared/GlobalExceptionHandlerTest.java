package com.wise.petadoption.shared;

import com.wise.petadoption.adoption.exception.ApplicationAccessDeniedException;
import com.wise.petadoption.shared.exception.ConflictException;
import com.wise.petadoption.shared.exception.NotFoundException;
import com.wise.petadoption.shared.storage.exception.ImageStorageException;
import com.wise.petadoption.user.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/resource");
        return request;
    }

    @Test
    void handleAuthorizationDenied_AnyRequest_ReturnsForbidden() {
        ResponseEntity<ApiErrorResponse> response = handler.handleAuthorizationDenied(request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path()).isEqualTo("/api/v1/resource");
    }

    @Test
    void handleAccessDenied_ApplicationAccessDenied_ReturnsForbiddenWithMessage() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleAccessDenied(new ApplicationAccessDeniedException(), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).contains("permission");
    }

    @Test
    void handleNotFound_NotFoundException_ReturnsNotFound() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotFound(new NotFoundException("missing"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("missing");
    }

    @Test
    void handleConflict_ConflictException_ReturnsConflictWithExceptionMessage() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleConflict(new ConflictException("already exists"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("already exists");
    }

    @Test
    void handleConflict_DataIntegrityViolation_ReturnsConflictWithGenericMessage() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleConflict(new DataIntegrityViolationException("constraint"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).contains("already exists or violates");
    }

    @Test
    void handleInvalidPassword_InvalidPasswordException_ReturnsBadRequest() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleInvalidPassword(new InvalidPasswordException("bad password"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("bad password");
    }

    @Test
    void handleMethodArgumentTypeMismatch_NoCause_ReturnsBadRequestWithFormattedMessage() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getCause()).thenReturn(null);
        when(ex.getValue()).thenReturn("abc");
        when(ex.getName()).thenReturn("status");

        ResponseEntity<ApiErrorResponse> response = handler.handleMethodArgumentTypeMismatch(ex, request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("abc").contains("status");
    }

    @Test
    void handleImageStorage_ImageStorageException_ReturnsInternalServerError() {
        ResponseEntity<ApiErrorResponse> response = handler.handleImageStorage(
                new ImageStorageException("storage failed", new RuntimeException()), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("storage failed");
    }

    @Test
    void handleUnexpected_GenericException_ReturnsInternalServerErrorWithoutLeakingDetails() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("leak"), request());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Internal server error");
    }
}
