package com.cyna.auth_users.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserDto {

    private String id;
    private String customerId;
    private String firstname;
    private String lastname;

    @Email(message = "L'email doit être valide")
    private String email;

    @Positive(message = "Le numéro de téléphone doit être positif")
    private Long phone;

    private String role;
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial"
    )

    @Schema(type = "string", format = "binary")
    private MultipartFile profile;

    private Boolean enabled;

    private String password;

    private Boolean emailVerified;
}
