package com.oceanview.service;

import com.oceanview.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO authenticate(String userName, String password);
}
