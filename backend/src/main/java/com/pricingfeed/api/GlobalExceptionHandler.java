package com.pricingfeed.api;

import com.pricingfeed.service.ApiExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiExceptions.NotFoundException.class)
    ResponseEntity<Map<String, Object>> handleNotFound(ApiExceptions.NotFoundException ex) {
        return err(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ApiExceptions.ForbiddenException.class)
    ResponseEntity<Map<String, Object>> handleForbidden(ApiExceptions.ForbiddenException ex) {
        return err(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
    }

    @ExceptionHandler(ApiExceptions.UnauthorizedException.class)
    ResponseEntity<Map<String, Object>> handleUnauthorized(ApiExceptions.UnauthorizedException ex) {
        return err(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    }

    @ExceptionHandler(ApiExceptions.BadRequestException.class)
    ResponseEntity<Map<String, Object>> handleBad(ApiExceptions.BadRequestException ex) {
        return err(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(ApiExceptions.ConflictException.class)
    ResponseEntity<Map<String, Object>> handleConflict(ApiExceptions.ConflictException ex) {
        return err(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return err(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return err(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> err(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "code", code,
                "message", message,
                "timestamp", OffsetDateTime.now().toString()
        ));
    }
}
