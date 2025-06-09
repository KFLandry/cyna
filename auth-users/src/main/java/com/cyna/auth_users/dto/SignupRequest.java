package com.cyna.auth_users.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String captchaToken; // Ajoutez ce champ
}
