package com.oceanview.validation.reservation;

import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.validation.ValidationStrategy;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateReservationValidationStrategy implements ValidationStrategy<CreateReservationDTO> {
    @Override
    public Map<String, String> validate(CreateReservationDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("reservation", "Reservation data is required");
            return errors;
        }
        // Guest ID validation
        if (dto.getGuestId() == null || dto.getGuestId() <= 0) {
            errors.put("guestId", "Valid guest ID is required");
        }

        // Date validation
        LocalDate checkIn = dto.getCheckInDate();
        LocalDate checkOut = dto.getCheckOutDate();

        if (checkIn == null) {
            errors.put("checkInDate", "Check-in date is required");
        }

        if (checkOut == null) {
            errors.put("checkOutDate", "Check-out date is required");
        }

        if (checkIn != null && checkOut != null) {
            if (!checkIn.isBefore(checkOut)) {
                errors.put("dateRange", "Check-in date must be before check-out date");
            }
        }

        // Room IDs validation
        List<Integer> roomIds = dto.getRoomIds();

        if (roomIds == null || roomIds.isEmpty()) {
            errors.put("roomIds", "At least one room must be selected");
        } else {
            // Check duplicates
            List<Integer> duplicates = roomIds.stream()
                    .collect(Collectors.groupingBy(id -> id, Collectors.counting()))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!duplicates.isEmpty()) {
                errors.put("roomIds", "Duplicate room IDs are not allowed");
            }

            // Check invalid IDs
            if (roomIds.stream().anyMatch(id -> id == null || id <= 0)) {
                errors.put("roomIds", "Room IDs must be valid positive integers");
            }
        }
        return errors;
    }

}
