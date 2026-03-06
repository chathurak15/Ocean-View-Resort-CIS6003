package com.oceanview.dao.impl;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.exception.DataAccessException;
import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;
import com.oceanview.model.Room;
import com.oceanview.model.enums.RoomType;
import com.oceanview.model.enums.Status;
import com.oceanview.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationDAOImpl implements ReservationDAO {
    // create reservation
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

    //exists overlapping reservation
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
            throw new DataAccessException("Error checking room availability", e);
        }
    }

    //get reservation by reservation no
    @Override
    public Reservation getByReservationNo(String reservationNo) {
        String sql = """
            SELECT 
                r.reservation_id, r.reservation_no, r.guest_id, r.check_in, r.check_out, r.status, r.total_amount, r.created_at,
                rr.rate_per_night, rr.line_total,
                rm.room_id, rm.room_name, rm.room_description, rm.room_price, rm.room_type, rm.available,
                g.guest_id AS g_id, g.name AS g_name, g.email AS g_email, g.phone_number AS g_phone,
                g.address AS g_address, g.nic AS g_nic, g.created_at AS g_created_at
            FROM reservations r
            JOIN reservation_rooms rr ON r.reservation_id = rr.reservation_id
            JOIN rooms rm ON rr.room_id = rm.room_id
            JOIN guests g ON r.guest_id = g.guest_id
            WHERE r.reservation_no = ?
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationNo);
            try (ResultSet rs = ps.executeQuery()) {
                Reservation reservation = null;
                List<ReservationRoom> reservationRooms = new ArrayList<>();
                while (rs.next()) {
                    if (reservation == null) {
                        reservation = new Reservation();
                        reservation.setReservationId(rs.getInt("reservation_id"));
                        reservation.setReservationNo(rs.getString("reservation_no"));
                        reservation.setCheckInDate(rs.getDate("check_in").toLocalDate());
                        reservation.setCheckOutDate(rs.getDate("check_out").toLocalDate());
                        reservation.setStatus(Status.valueOf(rs.getString("status")));
                        reservation.setTotalAmount(rs.getBigDecimal("total_amount"));
                        reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                        // Map guest from JOIN (NO GuestDAO call)
                        Guest guest = new Guest(
                                rs.getString("g_name"),
                                rs.getString("g_email"),
                                rs.getString("g_phone"),
                                rs.getString("g_address"),
                                rs.getString("g_nic")
                        );
                        guest.setGuestId(rs.getInt("g_id"));
                        Timestamp gCreated = rs.getTimestamp("g_created_at");
                        if (gCreated != null) {
                            guest.setCreatedAt(gCreated.toLocalDateTime());
                        }
                        reservation.setGuest(guest);
                    }

                    // Map room
                    Room room = new Room(
                            rs.getString("room_name"),
                            rs.getString("room_description"),
                            rs.getBigDecimal("room_price"),
                            RoomType.valueOf(rs.getString("room_type")),
                            rs.getBoolean("available")
                    );
                    room.assignId(rs.getInt("room_id"));

                    // Map reservation-room line
                    ReservationRoom rr = new ReservationRoom();
                    rr.setReservation(reservation);
                    rr.setRoom(room);
                    rr.setRatePerNight(rs.getBigDecimal("rate_per_night"));
                    rr.setLineTotal(rs.getBigDecimal("line_total"));

                    reservationRooms.add(rr);
                }

                if (reservation != null) {
                    reservation.setReservationRooms(reservationRooms);
                }
                return reservation;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving reservation", e);
        }
    }

    //update reservation status
    @Override
    public void updateStatus(String reservationNo, Status status) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_no = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, reservationNo);
            int affected = ps.executeUpdate();

            if (affected == 0) {
                throw new DataAccessException("Reservation not found for update");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating reservation status", e);
        }
    }

    @Override
    public void cancelViaStoredProcedure(String reservationNo) {
        String sql = "{CALL cancel_reservation(?)}";
        try (Connection conn = DBConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, reservationNo);
            cs.execute();
        } catch (SQLException e) {

            throw new RuntimeException(e.getMessage(), e);
        }
    }

    //get all reservations
    @Override
    public List<Reservation> getAllReservations() {
        String sql = """
SELECT r.reservation_id, r.reservation_no, r.check_in, r.check_out, r.status, r.total_amount, r.created_at,
           g.guest_id AS g_id, g.name AS g_name, g.email AS g_email, g.phone_number AS g_phone, g.address AS g_address,
           g.nic AS g_nic, g.created_at AS g_created_at,
           rr.reservation_room_id, rr.rate_per_night,
           rm.room_id, rm.room_name, rm.room_description, rm.room_price, rm.room_type, rm.available
    FROM reservations r
    JOIN guests g ON r.guest_id = g.guest_id
    LEFT JOIN reservation_rooms rr ON r.reservation_id = rr.reservation_id
    LEFT JOIN rooms rm ON rr.room_id = rm.room_id
    ORDER BY r.created_at DESC;
""";
        List<Reservation> reservations = new ArrayList<>();
        Map<Integer, Reservation> reservationMap = new HashMap<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                Reservation reservation = reservationMap.get(reservationId);
                if (reservation == null) {
                    reservation = new Reservation();
                    reservation.setReservationId(reservationId);
                    reservation.setReservationNo(rs.getString("reservation_no"));
                    reservation.setCheckInDate(rs.getDate("check_in").toLocalDate());
                    reservation.setCheckOutDate(rs.getDate("check_out").toLocalDate());
                    reservation.setStatus(Status.valueOf(rs.getString("status")));
                    reservation.setTotalAmount(rs.getBigDecimal("total_amount"));
                    reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    reservation.setReservationRooms(new ArrayList<>());

                    Guest guest = new Guest(
                            rs.getString("g_name"),
                            rs.getString("g_email"),
                            rs.getString("g_phone"),
                            rs.getString("g_address"),
                            rs.getString("g_nic")
                    );
                    guest.setGuestId(rs.getInt("g_id"));
                    Timestamp gCreated = rs.getTimestamp("g_created_at");
                    if (gCreated != null) {
                        guest.setCreatedAt(gCreated.toLocalDateTime());
                    }
                    reservation.setGuest(guest);
                    reservationMap.put(reservationId, reservation);
                    reservations.add(reservation);
                }
                int roomId = rs.getInt("room_id");
                if (roomId > 0) {
                    Room room = new Room(
                            rs.getString("room_name"),
                            rs.getString("room_description"),
                            rs.getBigDecimal("room_price"),
                            RoomType.valueOf(rs.getString("room_type")),
                            rs.getBoolean("available")
                    );
                    room.assignId(rs.getInt("room_id"));
                    ReservationRoom reservationRoom = new ReservationRoom();
                    reservationRoom.setReservationRoomId(rs.getInt("reservation_room_id"));
                    reservationRoom.setRoom(room);
                    reservationRoom.setRatePerNight(rs.getBigDecimal("rate_per_night"));
                    reservation.getReservationRooms().add(reservationRoom);
                }
            }
            return reservations;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving reservations", e);
        }
    }

    @Override
    public List<Reservation> findByDateRange(LocalDate from, LocalDate to) {
        String sql = """
        SELECT r.reservation_id, r.reservation_no, r.check_in, r.check_out, r.status, r.total_amount, r.created_at,
               g.guest_id AS g_id, g.name AS g_name, g.email AS g_email, g.phone_number AS g_phone,
               g.address AS g_address, g.nic AS g_nic, g.created_at AS g_created_at
        FROM reservations r
        JOIN guests g ON r.guest_id = g.guest_id
        WHERE (r.check_in < ? AND r.check_out > ?)   -- overlap rule
        ORDER BY r.check_in ASC
    """;

        List<Reservation> out = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(to));
            ps.setDate(2, Date.valueOf(from));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reservation r = new Reservation();
                    r.setReservationId(rs.getInt("reservation_id"));
                    r.setReservationNo(rs.getString("reservation_no"));
                    r.setCheckInDate(rs.getDate("check_in").toLocalDate());
                    r.setCheckOutDate(rs.getDate("check_out").toLocalDate());
                    r.setStatus(Status.valueOf(rs.getString("status")));
                    r.setTotalAmount(rs.getBigDecimal("total_amount"));
                    r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                    Guest g = new Guest(
                            rs.getString("g_name"),
                            rs.getString("g_email"),
                            rs.getString("g_phone"),
                            rs.getString("g_address"),
                            rs.getString("g_nic")
                    );
                    g.setGuestId(rs.getInt("g_id"));
                    Timestamp gCreated = rs.getTimestamp("g_created_at");
                    if (gCreated != null) g.setCreatedAt(gCreated.toLocalDateTime());
                    r.setGuest(g);

                    r.setReservationRooms(new ArrayList<>()); // keep empty for report list
                    out.add(r);
                }
            }

            return out;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving reservations by date range", e);
        }
    }
}
