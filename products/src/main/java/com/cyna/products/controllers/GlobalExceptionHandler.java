package com.cyna.products.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Gestion des erreurs de validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.BAD_REQUEST);
        errors.put("error", "Validation Error");

        // Récupération des messages d'erreur pour chaque champ invalide
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errors.put("validationErrors", fieldErrors);
        errors.put("message", "Des erreurs de validation ont été détectées");
        
        log.error("Validation errors: {}", fieldErrors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Gestion des exceptions liées aux fichiers trop grands
    @ExceptionHandler(MaxUploadSizeExceededException.class) 
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage(), ex);
        
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.PAYLOAD_TOO_LARGE);
        errors.put("error", "File Size Exceeded");
        errors.put("message", "La taille du fichier dépasse la limite autorisée");
        
        return new ResponseEntity<>(errors, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // Ajouter un gestionnaire spécifique pour les erreurs multipart
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, Object>> handleMultipartException(MultipartException ex) {
        log.error("Multipart error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.BAD_REQUEST);
        errors.put("error", "Multipart Error");
        errors.put("message", "Erreur lors du traitement des fichiers téléchargés: " + ex.getMessage());
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Gestion des exceptions HTTP standard
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", ex.getStatusCode());
        errors.put("error", ex.getReason());
        errors.put("message", ex.getReason());
        
        log.error("HTTP error: {} - {}", ex.getStatusCode(), ex.getReason());
        return new ResponseEntity<>(errors, ex.getStatusCode());
    }

    // Gestion générique des autres exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.BAD_REQUEST);
        errors.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errors.put("message", ex.getMessage());
        errors.put("exceptionType", ex.getClass().getSimpleName());
        
        log.error("Unhandled exception: ", ex);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
