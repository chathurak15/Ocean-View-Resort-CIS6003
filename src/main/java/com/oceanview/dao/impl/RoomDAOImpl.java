package com.oceanview.dao.impl;

import com.oceanview.dao.RoomDAO;
import com.oceanview.exception.DataAccessException;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.model.Room;
import com.oceanview.model.enums.RoomType;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAOImpl implements RoomDAO {
    @Override
    public List<Room> getAllRooms() {
        String sql = "SELECT room_id, room_name, room_description,room_price, room_type, available FROM rooms";

        List<Room> rooms = new ArrayList<>();

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Room room = new Room(
                        rs.getString("room_name"),
                        rs.getString("room_description"),
                        rs.getBigDecimal("room_price"),
                        RoomType.valueOf(rs.getString("room_type")),
                        rs.getBoolean("available")
                );
                room.assignId(rs.getInt("room_id"));
                rooms.add(room);
            }
            return rooms;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving rooms", e);
        }
    }

    @Override
    public Room createRoom(Room room) {

        String sql = "INSERT INTO rooms (room_name, room_description, room_price, room_type, available) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, room.getRoomName());
            ps.setString(2, room.getRoomDescription());
            ps.setBigDecimal(3, room.getRoomPrice());
            ps.setString(4, room.getRoomType().name());
            ps.setBoolean(5, room.isAvailable());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating room failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    room.assignId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating room failed, no ID obtained.");
                }
            }
            return room;

        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {  // MySQL duplicate constraint
                throw new DuplicateResourceException("Room name already exists.");
            }
            throw new DataAccessException("Error creating room", e);
        }
    }
}
