package com.oceanview.service;

import com.oceanview.dto.LoginRequestDTO;
import com.oceanview.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO authenticate(LoginRequestDTO loginRequestDTO);
}
