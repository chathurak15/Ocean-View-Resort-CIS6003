package com.oceanview.dao;

import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;
import com.oceanview.model.enums.Status;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeReservationDAO implements ReservationDAO {
    private final Map<String, Reservation> byNo = new HashMap<>();
    private final AtomicInteger idSeq = new AtomicInteger(1);

    // seed an existing reservation
    public void seed(Reservation reservation) {
        if (reservation.getReservationId() == null) {
            reservation.setReservationId(idSeq.getAndIncrement());
        }
        byNo.put(reservation.getReservationNo(), reservation);
    }

    @Override
    public Reservation createReservation(Reservation reservation) {
        if (reservation.getReservationId() == null) {
            reservation.setReservationId(idSeq.getAndIncrement());
        }
        byNo.put(reservation.getReservationNo(), reservation);
        return reservation;
    }

    @Override
    public boolean existsOverlappingReservation(Integer roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        if (roomId == null || checkInDate == null || checkOutDate == null) return false;
        for (Reservation r : byNo.values()) {
            if (r.getStatus() != Status.CONFIRMED) continue;
            if (r.getReservationRooms() == null) continue;

            boolean roomMatch = r.getReservationRooms().stream()
                    .map(ReservationRoom::getRoom)
                    .filter(Objects::nonNull)
                    .anyMatch(room -> Objects.equals(room.getRoomId(), roomId));
            if (!roomMatch) continue;

            // Overlap rule
            boolean overlap = checkInDate.isBefore(r.getCheckOutDate()) && checkOutDate.isAfter(r.getCheckInDate());
            if (overlap) return true;
        }
        return false;
    }

    @Override
    public Reservation getByReservationNo(String reservationNo) {
        return byNo.get(reservationNo);
    }

    @Override
    public void updateStatus(String reservationNo, Status status) {
        Reservation r = byNo.get(reservationNo);
        if (r == null) return;
        r.setStatus(status);
    }

    // In-memory equivalent of the MySQL stored procedure
    @Override
    public void cancelViaStoredProcedure(String reservationNo) {
        Reservation r = byNo.get(reservationNo);
        if (r == null)
            throw new RuntimeException("Reservation not found");
        if (r.getStatus() == Status.CANCELLED)
            throw new RuntimeException("Reservation is already cancelled");
        r.setStatus(Status.CANCELLED);
    }

    @Override
    public List<Reservation> getAllReservations() {
        // Keep stable ordering by reservationId
        List<Reservation> list = new ArrayList<>(byNo.values());
        list.sort(Comparator.comparing(Reservation::getReservationId));
        return list;
    }

    @Override
    public List<Reservation> findByDateRange(LocalDate from, LocalDate to) {
        return byNo.values().stream().filter(r -> r.getCreatedAt() != null).filter(r -> {
                        LocalDate d = r.getCreatedAt().toLocalDate();
                        return (!d.isBefore(from)) && (!d.isAfter(to));
        }).sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).toList();
    }
}
