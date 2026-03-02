package com.oceanview.mapper;

import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.dto.Reservation.ReservationDTO;
import com.oceanview.dto.Reservation.ReservationRoomDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;
import com.oceanview.model.Room;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationMapper {

    public static Reservation toEntity(CreateReservationDTO dto, GuestDTO guestDTO, List<RoomDTO> roomDTOs) {
        Reservation reservation = new Reservation();

        Guest guest = GuestMapper.toEntity(guestDTO);
        reservation.setGuest(guest);
        reservation.setCheckInDate(dto.getCheckInDate());
        reservation.setCheckOutDate(dto.getCheckOutDate());
        List<ReservationRoom> reservationRooms = roomDTOs.stream()
                .map(roomDTO -> {
                    Room room = RoomMapper.toEntity(roomDTO);
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
        dto.setGuestName(reservation.getGuest().getName());
        dto.setCheckInDate(reservation.getCheckInDate());
        dto.setCheckOutDate(reservation.getCheckOutDate());
        dto.setStatus(reservation.getStatus());
        dto.setTotalAmount(reservation.getTotalAmount());
        dto.setCreatedAt(reservation.getCreatedAt());

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
