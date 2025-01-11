package com.cyna.auth_users.auth.controller;

import com.cyna.auth_users.auth.dto.*;
import com.cyna.auth_users.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/validate")
    public  ResponseEntity<ValidationResult> validate(@RequestBody TokenValidationRequest request) {
        return ResponseEntity.ok(authService.validate(request.getToken()));

    }
}
