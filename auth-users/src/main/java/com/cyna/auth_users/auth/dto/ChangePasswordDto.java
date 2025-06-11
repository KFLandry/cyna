package com.cyna.auth_users.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotNull
    long userId;
    @NotBlank
    String oldPassword;
    @NotBlank
    String newPassword;
}

