package com.medilab.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Log the validation error for diagnostics
        logger.debug("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TrialExpiredException.class)
    @ResponseBody
    public ResponseEntity<Object> handleTrialExpired(TrialExpiredException ex) {
        logger.info("Trial expired: {}", ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("error", "trial_expired");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.PAYMENT_REQUIRED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("error", "access_denied");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseBody
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        logger.warn("Illegal state: {}", ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("error", "illegal_state");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        // Log the full exception on the server for diagnostics, but return a sanitized
        // message to the client.
        logger.error("Unhandled exception caught in GlobalExceptionHandler", ex);

        Map<String, String> body = new HashMap<>();
        body.put("error", "internal_server_error");
        body.put("message", "An unexpected error occurred.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
