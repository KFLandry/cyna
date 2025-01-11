package com.cyna.auth_users.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationResult {
    private boolean valid;
    private String username;
    private Date expiration;
    private String message;
}