package com.oceanview.mapper;

import com.oceanview.dto.UserDTO;
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

    public static User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setName(dto.getName());
        user.setUserName(dto.getUserName());
        user.setUserType(dto.getUserType());
        user.setActive(dto.isActive());
        return user;
    }
}
