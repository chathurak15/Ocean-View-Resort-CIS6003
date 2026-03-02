package com.oceanview.facade;

import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.dto.Reservation.ReservationDTO;

import java.util.List;

public interface ReservationFacade {
    ReservationDTO createReservation(CreateReservationDTO dto);
    ReservationDTO getByReservationNo(String reservationNo);
    void cancelReservation(String reservationNo);
    List<ReservationDTO> getAllReservations();
    void completeReservation(String reservationNo);
}
