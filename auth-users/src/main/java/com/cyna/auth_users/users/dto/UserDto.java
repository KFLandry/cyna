package com.cyna.auth_users.users.dto;

import com.cyna.auth_users.users.models.ROLE;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;

    private String customerId;

    private String firstname;

    private String lastname;

    private String email;

    private Boolean enabled;

    private Boolean emailVerified;

    private Long phone;

    private String urlProfile;

    private ROLE roles;
}
