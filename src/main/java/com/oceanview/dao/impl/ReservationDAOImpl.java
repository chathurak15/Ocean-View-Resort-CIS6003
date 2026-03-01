package com.oceanview.dao.impl;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;

public class ReservationDAOImpl implements ReservationDAO {

    //create reservation
    @Override
    public Reservation createReservation(Reservation reservation) {
        String insertReservationSql =
                "INSERT INTO reservations (reservation_no, guest_id, check_in, check_out, status, total_amount, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String insertReservationRoomSql =
                "INSERT INTO reservation_rooms (reservation_id, room_id, rate_per_night, line_total) " +
                        "VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            //TRANSACTION START
            conn.setAutoCommit(false);

            // Insert header
            try (PreparedStatement ps = conn.prepareStatement(insertReservationSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, reservation.getReservationNo());
                ps.setInt(2, reservation.getGuest().getGuestId());
                ps.setDate(3, Date.valueOf(reservation.getCheckInDate()));
                ps.setDate(4, Date.valueOf(reservation.getCheckOutDate()));
                ps.setString(5, reservation.getStatus().name());
                ps.setBigDecimal(6, reservation.getTotalAmount());
                ps.setTimestamp(7, Timestamp.valueOf(reservation.getCreatedAt()));

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Creating reservation failed, no rows affected.");
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int reservationId = keys.getInt(1);
                        reservation.setReservationId(reservationId);
                    } else {
                        throw new SQLException("Creating reservation failed, no ID obtained.");
                    }
                }
            }

            // Insert details
            try (PreparedStatement psRooms = conn.prepareStatement(insertReservationRoomSql)) {

                for (ReservationRoom rr : reservation.getReservationRooms()) {
                    if (rr.getRatePerNight() == null || rr.getLineTotal() == null) {
                        throw new IllegalArgumentException("Rate and line total must be calculated before saving");
                    }

                    psRooms.setInt(1, reservation.getReservationId());
                    psRooms.setInt(2, rr.getRoom().getRoomId());
                    psRooms.setBigDecimal(3, rr.getRatePerNight());
                    psRooms.setBigDecimal(4, rr.getLineTotal());

                    psRooms.addBatch();
                }
                psRooms.executeBatch();
            }

            // COMMIT
            conn.commit();
            return reservation;

        } catch (Exception e) {
            //ROLLBACK BEST EFFORT
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new RuntimeException("Error creating reservation", e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignore) {
                }

                try {
                    conn.close();
                } catch (SQLException ignore) {
                }
            }
        }
    }

    @Override
    public boolean existsOverlappingReservation(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        String sql =
                "SELECT COUNT(*) FROM reservation_rooms rr " +
                        "JOIN reservations r ON rr.reservation_id = r.reservation_id " +
                        "WHERE rr.room_id = ? AND r.status = 'CONFIRMED' " +
                        "  AND (? < r.check_out AND ? > r.check_in)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(checkInDate));
            ps.setDate(3, Date.valueOf(checkOutDate));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error checking room availability", e);
        }
    }
}
