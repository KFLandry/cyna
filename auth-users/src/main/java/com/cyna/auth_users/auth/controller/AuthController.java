package com.cyna.auth_users.auth.controller;

import com.cyna.auth_users.auth.dto.*;
import com.cyna.auth_users.auth.service.AuthService;
import com.cyna.auth_users.users.dto.UserDto;
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
    public ResponseEntity<?> signup(@Valid @RequestBody CreateUserDto userDto) {
        return ResponseEntity.ok(authService.register(userDto));
    }

    @PostMapping("/validate")
    public  ResponseEntity<ValidationResult> validate(@Valid @RequestBody TokenValidationRequest request) {
        return ResponseEntity.ok(authService.validate(request.getToken()));
    }
}
