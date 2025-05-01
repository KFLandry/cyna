package com.cyna.auth_users.auth.controller;

import com.cyna.auth_users.auth.dto.*;
import com.cyna.auth_users.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody CreateUserDto userDto) {
        return ResponseEntity.ok(authService.register(userDto));
    }

    @PostMapping("/validate")
    public  ResponseEntity<ValidationResult> validate(@Valid @RequestBody TokenValidationRequest request) {
        return ResponseEntity.ok(authService.validate(request.getToken()));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.verifyEmail(email));
    }

    @GetMapping("/validate-email")
    public ResponseEntity<String> validateEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.validateEmail(email));
    }

    @GetMapping("/validate-account")
    public ResponseEntity<String> validateAccount(@RequestParam String email) {
        return ResponseEntity.ok(authService.validateAccount(email));
    }
}
