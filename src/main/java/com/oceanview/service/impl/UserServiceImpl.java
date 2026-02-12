package com.oceanview.service.impl;

import com.oceanview.dao.UserDAO;
import com.oceanview.dao.impl.UserDAOImpl;
import com.oceanview.dto.LoginRequestDTO;
import com.oceanview.dto.UserDTO;
import com.oceanview.mapper.UserMapper;
import com.oceanview.model.User;
import com.oceanview.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userDAO.findAll();
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());

    }

    @Override
    public UserDTO authenticate(LoginRequestDTO loginRequestDTO) {
        if (loginRequestDTO.getUserName() == null || loginRequestDTO.getUserName() .isBlank() || loginRequestDTO.getPassword() == null) {
            return null;
        }

        Optional<User> userOpt = userDAO.findActiveByUsername(loginRequestDTO.getUserName());
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        if (!loginRequestDTO.getPassword().equals(user.getPassword())){
            return null;
        }

        return UserMapper.toDTO(user);
    }
}
