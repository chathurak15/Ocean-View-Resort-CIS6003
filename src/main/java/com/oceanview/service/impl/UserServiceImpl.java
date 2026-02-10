package com.oceanview.service.impl;

import com.oceanview.dto.UserDTO;
import com.oceanview.service.UserService;

import java.util.List;

public class UserServiceImpl implements UserService {
    @Override
    public List<UserDTO> getAllUsers() {
        return List.of();
    }

    @Override
    public UserDTO authenticate(String userName, String password) {
        return null;
    }
}
