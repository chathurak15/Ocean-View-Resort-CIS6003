package com.oceanview.dao;

import com.oceanview.model.Reservation;

import java.time.LocalDate;

public interface ReservationDAO {
    Reservation createReservation(Reservation reservation);

    boolean existsOverlappingReservation(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate);
}
