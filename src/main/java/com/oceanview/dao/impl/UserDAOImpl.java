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
