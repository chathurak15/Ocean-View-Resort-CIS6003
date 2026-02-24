package com.oceanview.dao.impl;

import com.oceanview.dao.GuestDAO;
import com.oceanview.model.Guest;
import com.oceanview.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GuestDAOImpl implements GuestDAO {
    @Override
    public List<Guest> getAllGuests() {
        String sql = "SELECT * FROM guests";
        List<Guest> guests = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Guest guest = new Guest(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getString("nic")
                );

                guest.setGuestId(rs.getInt("guest_id"));
                guests.add(guest);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving guests", e);
        }
        return guests;
    }
}
