package com.oceanview.dao;

import com.oceanview.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    User createUser(User entity);
    List<User> findAll();
    Optional<User> findActiveByUsername(String username);
    boolean deleteById(int userId);
    Optional<User> findById(int id);
    boolean updateActiveStatus(int userId, boolean isActive);
}
