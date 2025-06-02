package com.cyna.auth_users.auth.service;

import com.cyna.auth_users.auth.dto.AuthResponse;
import com.cyna.auth_users.auth.dto.CreateUserDto;
import com.cyna.auth_users.auth.dto.LoginRequest;
import com.cyna.auth_users.auth.dto.ValidationResult;
import com.cyna.auth_users.users.models.ROLE;
import com.cyna.auth_users.users.models.User;
import com.cyna.auth_users.users.repositories.UserRepository;
import com.cyna.auth_users.users.service.MailerSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    private final MailerSendService mailerSendService;

    @Value("${mailerSend.super_admin}")
    private String superAdminEmail;

    @Value("${endpoints.validate_email}")
    private String validateEmailEndpoint;

    @Value("${endpoints.validate_account}")
    private String validateAccountEndpoint;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = repository.findByEmail(request.getEmail()).orElseThrow();

        if (user.getRoles().equals(ROLE.ADMIN) && Boolean.FALSE.equals(user.getEnabled()))
            throw new ResponseStatusException(HttpStatusCode.valueOf(HttpStatus.SC_FORBIDDEN), "Account not activate");

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


    public Object register(CreateUserDto request) {
        //Pour les comptes Admins, un mail de validation de création de compte est envoyé au super ADMIN, la validation des emails
        if (ROLE.valueOf(request.getRole()).equals(ROLE.ADMIN)) {
            try {
                mailerSendService.sendEmail(superAdminEmail, validateAccountEndpoint+"?email="+request.getEmail(), "validate.account", request.getLastname() + " " + request.getFirstname());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(ROLE.valueOf(request.getRole()))
                .enabled(!(ROLE.valueOf(request.getRole()).equals(ROLE.ADMIN)))
                .emailVerified(false)
                .build();
        repository.save(user);

        if (ROLE.valueOf(request.getRole()).equals(ROLE.ADMIN))
            return "Your admin account has been created. An email has been sent to the Super Admin for validation";


        String jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public String validateEmail(String email) {

        User user = repository.findByEmail(email).orElseThrow();
        user.setEmailVerified(true);
        repository.save(user);

        return "Email verified";
    }

    public String validateAccount(String email) {
        User user = repository.findByEmail(email).orElseThrow();
        user.setEnabled(true);
        repository.save(user);

        return "Account linked to "+email+" has been activated";
    }

    public String verifyEmail(String email) {
        try {
            mailerSendService.sendEmail(email, validateEmailEndpoint+"?email="+email, "validate.email");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "Email Sent";
    }
}
