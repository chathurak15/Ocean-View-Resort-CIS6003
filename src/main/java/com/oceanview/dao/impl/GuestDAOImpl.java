package com.oceanview.dao.impl;

import com.oceanview.dao.GuestDAO;
import com.oceanview.model.Guest;
import com.oceanview.service.search.GuestSearchCriteria;
import com.oceanview.util.DBConnection;

import java.sql.*;
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
                guest.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                guests.add(guest);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving guests", e);
        }
        return guests;
    }

    @Override
    public Guest addGuest(Guest guest) {
        String sql = "INSERT INTO guests (name, email, phone_number, address, nic) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, guest.getName());
            stmt.setString(2, guest.getEmail());
            stmt.setString(3, guest.getPhoneNumber());
            stmt.setString(4, guest.getAddress());
            stmt.setString(5, guest.getNic());

            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                guest.setGuestId(keys.getInt(1));
            }
            return guest;

        } catch (SQLException e) {
            throw new RuntimeException("Error creating guest", e);
        }
    }

    //search guests
    @Override
    public List<Guest> search(GuestSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT * FROM guests WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (criteria.hasGuestId()){
            sql.append("AND guest_id = ? ");
            params.add(criteria.getGuestId());
        }
        if (criteria.hasNic()){
            sql.append("AND nic = ? ");
            params.add(criteria.getNic());
        }

        if (criteria.hasPhoneNumber()) {
            sql.append("AND phone_number = ? ");
            params.add(criteria.getPhoneNumber());
        }
        List<Guest> guests = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())){
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Guest guest = new Guest(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getString("address"),
                        rs.getString("nic")
                );
                guest.setGuestId(rs.getInt("guest_id"));
                guest.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                guests.add(guest);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Error searching guests", ex);
        }
        return guests;
    }
}
