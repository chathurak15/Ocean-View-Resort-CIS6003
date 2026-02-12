package com.oceanview.service;

import com.oceanview.dto.user.LoginRequestDTO;
import com.oceanview.dto.user.RegisterDTO;
import com.oceanview.dto.user.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();

    UserDTO authenticate(LoginRequestDTO loginRequestDTO);

    UserDTO createUser(RegisterDTO registerDTO);

    boolean deleteUser(int userId);

    boolean updateUserStatus(int userId, boolean active);
}
