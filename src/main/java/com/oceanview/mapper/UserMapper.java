package com.oceanview.mapper;

import com.oceanview.dto.user.RegisterDTO;
import com.oceanview.dto.user.UserDTO;
import com.oceanview.model.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setName(user.getName());
        dto.setUserName(user.getUserName());
        dto.setUserType(user.getUserType());
        dto.setActive(user.isActive());
        return dto;
    }

    // Register Request DTO -> Entity
    public static User toEntity(RegisterDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setName(dto.getName());
        user.setUserName(dto.getUserName());
        user.setPassword(dto.getPassword());
        user.setUserType(dto.getUserType());
        user.setActive(true);
        return user;
    }
}
