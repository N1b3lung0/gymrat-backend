package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest;

import com.n1b3lung0.gymrat.domain.exception.BusinessRuleViolationException;
import com.n1b3lung0.gymrat.domain.exception.ConflictException;
import com.n1b3lung0.gymrat.domain.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.Map;

/**
 * Global REST exception handler following RFC 9457 (Problem Details for HTTP APIs).
 *
 * <p>All error responses use {@code Content-Type: application/problem+json}.
 * The {@code type} URI acts as a stable, machine-readable error identifier.
 *
 * <p>Mapping summary:
 * <ul>
 *   <li>{@link NotFoundException}                  → {@code 404 Not Found}</li>
 *   <li>{@link ConflictException}                  → {@code 409 Conflict}</li>
 *   <li>{@link BusinessRuleViolationException}     → {@code 422 Unprocessable Entity}</li>
 *   <li>{@link MethodArgumentNotValidException}    → {@code 422 Unprocessable Entity} + field violations</li>
 *   <li>{@link MethodArgumentTypeMismatchException}→ {@code 400 Bad Request} (invalid UUID / enum path var)</li>
 *   <li>{@link HttpMessageNotReadableException}    → {@code 400 Bad Request} (malformed JSON)</li>
 *   <li>{@link Exception}                          → {@code 500 Internal Server Error} (catch-all)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String BASE_TYPE = "https://gymrat.api/errors/";

    // -------------------------------------------------------------------------
    // 404 — Not Found
    // -------------------------------------------------------------------------

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(typeUri(ex));
        problem.setTitle("Resource Not Found");
        return problem;
    }

    // -------------------------------------------------------------------------
    // 409 — Conflict
    // -------------------------------------------------------------------------

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(typeUri(ex));
        problem.setTitle("Conflict");
        return problem;
    }

    // -------------------------------------------------------------------------
    // 422 — Business Rule Violation
    // -------------------------------------------------------------------------

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleViolationException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(typeUri(ex));
        problem.setTitle("Business Rule Violation");
        return problem;
    }

    // -------------------------------------------------------------------------
    // 422 — Bean Validation (@Valid)
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {} field error(s)", ex.getBindingResult().getErrorCount());
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        problem.setType(URI.create(BASE_TYPE + "validation-failed"));
        problem.setTitle("Validation Failed");
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
                .toList());
        return problem;
    }

    // -------------------------------------------------------------------------
    // 400 — Bad Request: invalid path variable type (UUID / enum)
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        var detail = "Invalid value '%s' for parameter '%s'".formatted(String.valueOf(ex.getValue()), ex.getName());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(URI.create(BASE_TYPE + "invalid-parameter"));
        problem.setTitle("Invalid Parameter");
        return problem;
    }

    // -------------------------------------------------------------------------
    // 400 — Bad Request: malformed JSON body
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Malformed or unreadable request body");
        problem.setType(URI.create(BASE_TYPE + "malformed-request"));
        problem.setTitle("Malformed Request");
        return problem;
    }

    // -------------------------------------------------------------------------
    // 500 — Catch-all
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setType(URI.create(BASE_TYPE + "internal-error"));
        problem.setTitle("Internal Server Error");
        return problem;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Derives the problem {@code type} URI from the exception class name.
     * e.g. {@code ExerciseNotFoundException} → {@code .../exercise-not-found}
     */
    private static URI typeUri(RuntimeException ex) {
        var slug = ex.getClass().getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase()
                .replace("exception", "")
                .replaceAll("-$", "");
        return URI.create(BASE_TYPE + slug);
    }
}

