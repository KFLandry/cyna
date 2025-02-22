package com.cyna.auth_users.auth.service;

import com.cyna.auth_users.auth.dto.AuthResponse;
import com.cyna.auth_users.auth.dto.CreateUserDto;
import com.cyna.auth_users.auth.dto.LoginRequest;
import com.cyna.auth_users.users.dto.UserDto;
import com.cyna.auth_users.auth.dto.ValidationResult;
import com.cyna.auth_users.users.repositories.UserRepository;
import com.cyna.auth_users.users.models.ROLE;
import com.cyna.auth_users.users.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = repository.findByEmail(request.getEmail()).orElseThrow();
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .build();
    }

    public ValidationResult validate(String token) {
        try {
            String username = jwtService.extractUsername(token);
            if (username == null) {
                return ValidationResult.builder().valid(false).message("User not found").build();
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, userDetails)) {
                Date expiration = jwtService.extractExpiration(token);
                return ValidationResult.builder()
                        .valid(true)
                        .username(username)
                        .expiration(expiration)
                        .message("valid token")
                        .build();
            }
            return ValidationResult.builder().valid(false).message("Invalid token").build();
        }catch (Exception e) {
            log.error("Error while validated token", e);
            return ValidationResult.builder().valid(false).message("Invalid token").build();
        }
    }

    public void logout(String token) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(token, null));
    }


    public AuthResponse register(CreateUserDto request) {
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(ROLE.valueOf(request.getRole()))
                .enabled(true)
                .emailVerified(false)
                .build();
        repository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}
