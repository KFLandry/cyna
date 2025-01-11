package com.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResult {
    private boolean valid;
    private String username;
    private Date expiration;
    private String message;
}