package com.cyna.auth_users.auth.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginRequest {
    private String email;
    private String password;
}

