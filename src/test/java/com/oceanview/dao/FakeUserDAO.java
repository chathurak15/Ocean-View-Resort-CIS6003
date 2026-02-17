package com.oceanview.dao;

import com.oceanview.model.User;

import java.util.*;

public class FakeUserDAO implements UserDAO{
    private final Map<Integer, User> store = new HashMap<>();
    private int idCounter = 1;

    // Helper only for tests (not part of UserDAO)
    public void seed(User user) {
        if (user.getUserId() == 0) {
            user.setUserId(idCounter++);
        }
        store.put(user.getUserId(), user);
    }

    @Override
    public User createUser(User entity) {
        entity.setUserId(idCounter++);
        store.put(entity.getUserId(), entity);
        return entity;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<User> findActiveByUsername(String username) {
        return store.values().stream()
                .filter(u -> u.isActive() && u.getUserName().equals(username))
                .findFirst();
    }

    @Override
    public boolean deleteById(int userId) {
        return store.remove(userId) != null;
    }

    @Override
    public Optional<User> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean updateActiveStatus(int userId, boolean isActive) {
        User u = store.get(userId);
        if (u == null) return false;
        u.setActive(isActive);
        return true;
    }
}
