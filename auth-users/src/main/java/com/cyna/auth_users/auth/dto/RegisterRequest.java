package com.cyna.auth_users.auth.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RegisterRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String role;
    private String password;

}
