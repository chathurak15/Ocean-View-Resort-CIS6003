package com.oceanview.mapper;

import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.dto.Reservation.ReservationDTO;
import com.oceanview.dto.Reservation.ReservationRoomDTO;
import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;
import com.oceanview.model.Room;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationMapper {

    public static Reservation toEntity(CreateReservationDTO dto, Guest guest, List<Room> rooms) {
        Reservation reservation = new Reservation();

        reservation.setGuest(guest);
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        List<ReservationRoom> reservationRooms = rooms.stream()
                .map(room -> {
                    ReservationRoom rr = new ReservationRoom();
                    rr.setReservation(reservation);
                    rr.setRoom(room);
                    rr.setRatePerNight(room.getRoomPrice());
                    rr.setLineTotal(BigDecimal.ZERO);
                    return rr;
                })
                .collect(Collectors.toList());
        reservation.setReservationRooms(reservationRooms);
        return reservation;
    }

    // Reservation -> Response DTO
    public static ReservationDTO toDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setReservationNo(reservation.getReservationNo());
        dto.setGuestId(reservation.getGuest().getGuestId());
        dto.setCheckInDate(reservation.getCheckInDate());
        dto.setCheckOutDate(reservation.getCheckOutDate());
        dto.setStatus(reservation.getStatus());
        dto.setTotalAmount(reservation.getTotalAmount());

        List<ReservationRoomDTO> roomDTOs = reservation.getReservationRooms()
                .stream()
                .map(ReservationMapper::toRoomDTO)
                .collect(Collectors.toList());

        dto.setRooms(roomDTOs);
        return dto;
    }


    // ReservationRoom -> DTO
    private static ReservationRoomDTO toRoomDTO(ReservationRoom rr) {
        ReservationRoomDTO dto = new ReservationRoomDTO();
        dto.setRoomId(rr.getRoom().getRoomId());
        dto.setRoomName(rr.getRoom().getRoomName());
        dto.setRatePerNight(rr.getRatePerNight());
        dto.setLineTotal(rr.getLineTotal());
        return dto;
    }
}
