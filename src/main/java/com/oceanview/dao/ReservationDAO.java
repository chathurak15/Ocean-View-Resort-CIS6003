package com.oceanview.dao;

import com.oceanview.model.Reservation;
import com.oceanview.model.enums.Status;

import java.time.LocalDate;
import java.util.List;

public interface ReservationDAO {
    Reservation createReservation(Reservation reservation);

    boolean existsOverlappingReservation(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate);

    Reservation getByReservationNo(String reservationNo);

    void updateStatus(String reservationNo, Status status);

    void cancelViaStoredProcedure(String reservationNo);

    List<Reservation> getAllReservations();

    List<Reservation> findByDateRange(LocalDate fromDate, LocalDate toDate);
}
