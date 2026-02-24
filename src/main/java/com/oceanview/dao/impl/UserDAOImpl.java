package com.oceanview.dao.impl;
import com.oceanview.dao.UserDAO;
import com.oceanview.model.User;
import com.oceanview.model.enums.UserType;
import com.oceanview.util.DBConnection;
import java.sql.Connection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    //create user dao impl
    @Override
    public User createUser(User entity) {
        String sql = "INSERT INTO users (name, user_name, password, user_type, is_active) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getName());
            ps.setString(2, entity.getUserName());
            ps.setString(3, entity.getPassword());
            ps.setString(4, entity.getUserType().name());
            ps.setBoolean(5, entity.isActive());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setUserId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

            return entity;

        } catch (SQLException e) {
            throw new RuntimeException("DB error while creating user", e);
        }
    }

    //get all users
    @Override
    public List<User> findAll() {
        String sql = "SELECT id, name, user_name,password, user_type, is_active FROM users";
        List<User> users = new ArrayList<>();
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException("DB error while fetching users", e);
        }
    }

    //find active user by username
    @Override
    public Optional<User> findActiveByUsername(String username) {
        String sql = "SELECT id, name, user_name, password, user_type, is_active " +
                "FROM users WHERE user_name = ? AND is_active = 1";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error while authenticating user", e);
        }
    }

    //delete user by id
    @Override
    public boolean deleteById(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("DB error while deleting user", e);
        }
    }

    //find user by id
    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT id, name, user_name, password, user_type, is_active FROM users WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error while finding user", e);
        }
    }

    //update user active status by user id
    @Override
    public boolean updateActiveStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, isActive);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("DB error while updating user status", e);
        }
    }


    //map row to a user object
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setUserName(rs.getString("user_name"));
        user.setPassword(rs.getString("password"));
        user.setUserType(UserType.valueOf(rs.getString("user_type")));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }
}
