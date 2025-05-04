package com.cyna.auth_users.users.dto;

import com.cyna.auth_users.users.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User UserDtoToUserDto(User user);

    UserDto UserToUserDto(User user);
}
