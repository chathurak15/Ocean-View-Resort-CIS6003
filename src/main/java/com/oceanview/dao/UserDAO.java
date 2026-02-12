package com.oceanview.dao;

import com.oceanview.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    List<User> findAll();
    Optional<User> findActiveByUsername(String username);
}
