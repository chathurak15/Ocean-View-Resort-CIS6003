package com.oceanview.facade.impl;
import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.dto.Reservation.ReservationDTO;
import com.oceanview.facade.ReservationFacade;


import java.util.List;

public class ReservationFacadeImpl implements ReservationFacade {
    @Override
    public ReservationDTO createReservation(CreateReservationDTO dto) {
        return null;
    }

    @Override
    public ReservationDTO getByReservationNo(String reservationNo) {
        return null;
    }

    @Override
    public void cancelReservation(String reservationNo) {

    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        return List.of();
    }
}
