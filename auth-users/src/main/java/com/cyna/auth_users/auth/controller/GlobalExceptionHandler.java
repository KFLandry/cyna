package com.cyna.auth_users.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des erreurs de validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", ex.getStatusCode());
        errors.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());

        // Récupération des messages d'erreur pour chaque champ invalide
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        errors.put("message", "Erreur de validation");
        errors.put("errors", fieldErrors);
        return new ResponseEntity<>(errors, ex.getStatusCode());
    }

    // Gestion générique des autres exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", ex instanceof ResponseStatusException ? ((ResponseStatusException) ex).getStatusCode() : HttpStatus.BAD_REQUEST);
        errors.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errors.put("message", ex.getMessage());

        return new ResponseEntity<>(errors, ex instanceof ResponseStatusException ? ((ResponseStatusException) ex).getStatusCode() : HttpStatus.BAD_REQUEST);
    }
}
