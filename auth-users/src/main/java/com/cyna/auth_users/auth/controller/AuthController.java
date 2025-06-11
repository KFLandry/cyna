package com.cyna.auth_users.auth.controller;

import com.cyna.auth_users.auth.dto.*;
import com.cyna.auth_users.auth.service.AuthService;
import com.cyna.auth_users.users.dto.UpdateUserDto;
import com.cyna.auth_users.users.dto.UserDto;
import com.cyna.auth_users.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final UserService userService;

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
    public ResponseEntity<String> verifyEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(authService.verifyEmail(email));
    }

    @GetMapping("/validate-email")
    public ResponseEntity<String> validateEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(authService.validateEmail(email));
    }

    @GetMapping("/validate-account")
    public ResponseEntity<String> validateAccount(@RequestParam("email") String email) {
        return ResponseEntity.ok(authService.validateAccount(email));
    }

    @GetMapping(value = "/password-forgot")
    public ResponseEntity<String> passwordForgot(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.passwordForget(email));
    }

    @PostMapping("/password-forgot/{userId}")
    public ResponseEntity<String> passwordForgot(@Valid @PathVariable("userId") long userId, @RequestBody UpdateUserDto user) {
        return ResponseEntity.ok(userService.update(userId, user));
    }

    //
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordDto dto) {
        authService.changePassword(dto);
        return ResponseEntity.noContent().build();
    }
}
