package com.cyna.auth_users.auth.dto;

import com.cyna.auth_users.users.models.ROLE;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserDto {

    @NotBlank(message = "Le prenom ne peut être vide")
    @NotNull(message = "Le prenom est obligatoire")
    private String firstname;

    @NotBlank(message = "Le nom de famille ne peut être vide")
    @NotNull(message = "Le nom de famille est obligatoire")
    private String lastname;

    @NotBlank(message = "Email cannot be blank")
    @NotNull(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = " Le role ne peut être vide")
    @NotNull(message = " Le role est obligatoire")
    private String role;

    @NotBlank(message = "Le mot de passe ne peut être vide")
    @NotNull(message = "Le mot de passe est obligatoire")
    private String password;

}
