package com.oceanview.facade.impl;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.dto.Reservation.ReservationDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.exception.BusinessRuleException;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.exception.ResourceNotFoundException;
import com.oceanview.facade.ReservationFacade;
import com.oceanview.mapper.ReservationMapper;
import com.oceanview.model.Reservation;
import com.oceanview.model.enums.Status;
import com.oceanview.service.GuestService;
import com.oceanview.service.RoomService;
import com.oceanview.service.billing.BillingStrategy;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ReservationFacadeImpl implements ReservationFacade {
    private final GuestService guestService;
    private final RoomService roomService;
    private final ReservationDAO reservationDAO;
    private final BillingStrategy billingStrategy;

    public ReservationFacadeImpl(GuestService guestService, RoomService roomService, ReservationDAO reservationDAO, BillingStrategy billingStrategy) {
        this.guestService = guestService;
        this.roomService = roomService;
        this.reservationDAO = reservationDAO;
        this.billingStrategy = billingStrategy;
    }

    //create reservation
    @Override
    public ReservationDTO createReservation(CreateReservationDTO dto) {
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        if (!dto.getCheckInDate().isBefore(dto.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-in date must be before check-out date.");
        }
        if (dto.getRoomIds() == null || dto.getRoomIds().isEmpty()) {
            throw new IllegalArgumentException("At least one room must be selected.");
        }

        GuestDTO guestDTO = guestService.getGuestById(dto.getGuestId());
        if (guestDTO == null) {
            throw new ResourceNotFoundException("Guest not found.");
        }
        List<RoomDTO> roomDTOs = new ArrayList<>();
        for (Integer roomId : dto.getRoomIds()) {
            RoomDTO roomDTO = roomService.getRoomById(roomId);
            if (roomDTO == null) throw new ResourceNotFoundException("Room not found: " + roomId);
            if (!roomDTO.isAvailable()) {
                throw new BusinessRuleException("Room is out of service (maintenance).");
            }
            roomDTOs.add(roomDTO);
        }
        for (RoomDTO roomDTO : roomDTOs) {
            boolean conflict = reservationDAO.existsOverlappingReservation(roomDTO.getRoomId(), dto.getCheckInDate(), dto.getCheckOutDate()
            );
            if (conflict) {
                throw new DuplicateResourceException("Room not available: " + roomDTO.getRoomName());
            }
        }
        Reservation reservation = ReservationMapper.toEntity(dto, guestDTO, roomDTOs);
        reservation.setReservationNo(generateReservationNo());
        reservation.setStatus(Status.CONFIRMED);
        reservation.setCreatedAt(LocalDateTime.now());

        billingStrategy.applyPricing(reservation);

        Reservation saved = reservationDAO.createReservation(reservation);
        return ReservationMapper.toDTO(saved);
    }

    //get reservation by reservation number
    @Override
    public ReservationDTO getByReservationNo(String reservationNo) {
        if (reservationNo == null || reservationNo.isBlank()) {
            throw new BusinessRuleException("Reservation number is required.");
        }

        Reservation reservation = reservationDAO.getByReservationNo(reservationNo);

        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation not found: " + reservationNo);
        }

        return ReservationMapper.toDTO(reservation);
    }

    //get all reservations
    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationDAO.getAllReservations();
        return reservations.stream().map(ReservationMapper::toDTO).toList();
    }

    //cancel reservation
    @Override
    public void cancelReservation(String reservationNo) {
        if (reservationNo == null || reservationNo.isBlank()) {
            throw new BusinessRuleException("Reservation number is required.");
        }
        try {
            reservationDAO.cancelViaStoredProcedure(reservationNo);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("not found")) {
                throw new ResourceNotFoundException(msg);
            }
            if (msg != null && msg.contains("already cancelled")) {
                throw new BusinessRuleException(msg);
            }
            throw e;
        }
    }

    @Override
    public void completeReservation(String reservationNo) {
        if (reservationNo == null || reservationNo.isBlank()) {
            throw new BusinessRuleException("Reservation number is required.");
        }
        Reservation reservation = reservationDAO.getByReservationNo(reservationNo);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reservation not found: " + reservationNo);
        }
        if (reservation.getStatus() == Status.COMPLETED) {
            throw new BusinessRuleException("Reservation is already COMPLETED.");
        }
        reservation.setStatus(Status.COMPLETED);
        reservationDAO.updateStatus(reservationNo, Status.COMPLETED);
    }

    @Override
    public List<ReservationDTO> getReservationsByDateRange(String from, String to) {
        if (from == null || to == null || from.isBlank() || to.isBlank()) {
            throw new BusinessRuleException("Both 'from' and 'to' dates are required.");
        }
        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(from.trim());
            toDate = LocalDate.parse(to.trim());
        } catch (Exception e) {
            throw new BusinessRuleException("Invalid date format. Use YYYY-MM-DD.");
        }
        // inclusive range rule
        if (fromDate.isAfter(toDate)) {
            throw new BusinessRuleException("'from' must be <= 'to'.");
        }

        List<Reservation> reservations = reservationDAO.findByDateRange(fromDate, toDate);
        return reservations.stream().map(ReservationMapper::toDTO).toList();
    }

    private String generateReservationNo() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
