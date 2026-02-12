package com.oceanview.service.impl;

import com.oceanview.dao.UserDAO;
import com.oceanview.dao.impl.UserDAOImpl;
import com.oceanview.dto.user.LoginRequestDTO;
import com.oceanview.dto.user.RegisterDTO;
import com.oceanview.dto.user.UserDTO;
import com.oceanview.exception.ForbiddenOperationException;
import com.oceanview.mapper.UserMapper;
import com.oceanview.model.User;
import com.oceanview.model.enums.UserType;
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
        if (loginRequestDTO.getUserName() == null || loginRequestDTO.getUserName().isBlank() || loginRequestDTO.getPassword() == null) {
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

    //create user
    @Override
    public UserDTO createUser(RegisterDTO registerDTO) {
        if (registerDTO.getUserName() == null || registerDTO.getUserName().isBlank() || registerDTO.getPassword().isEmpty()) {
            return null;
        }
        if (userDAO.findActiveByUsername(registerDTO.getUserName()).isPresent()) {
            return null;
        }

        User user = userDAO.createUser(UserMapper.toEntity(registerDTO));
        return UserMapper.toDTO(user);
    }

    //delete user(user is ADMINISTRATOR, reject delete & and return Exception)
    @Override
    public boolean deleteUser(int userId) {
        User user = userDAO.findById(userId).orElse(null);
        if (user == null) return false;

        if (user.getUserType() == UserType.ADMINISTRATOR) {
            throw new ForbiddenOperationException("Admin user cannot be deleted");
        }
        return userDAO.deleteById(userId);
    }
}
