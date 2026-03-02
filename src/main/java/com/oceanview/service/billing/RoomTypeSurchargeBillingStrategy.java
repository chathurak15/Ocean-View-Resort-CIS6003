package com.oceanview.service.billing;

import com.oceanview.exception.BusinessRuleException;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;
import com.oceanview.model.enums.RoomType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

public class RoomTypeSurchargeBillingStrategy implements BillingStrategy{

    @Override
    public void applyPricing(Reservation reservation) {

        if (reservation == null) {
            throw new BusinessRuleException("Reservation cannot be null");
        }

        if (reservation.getReservationRooms() == null || reservation.getReservationRooms().isEmpty()) {
            throw new BusinessRuleException("Reservation must contain at least one room.");
        }

        long nights = ChronoUnit.DAYS.between(
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        if (nights <= 0) {
            throw new BusinessRuleException("Check-out date must be after check-in date.");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ReservationRoom rr : reservation.getReservationRooms()) {

            if (rr.getRoom() == null || rr.getRoom().getRoomType() == null) {
                throw new BusinessRuleException("Room type is required for billing.");
            }

            BigDecimal baseRate = rr.getRatePerNight();
            if (baseRate == null || baseRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Invalid room rate.");
            }

            BigDecimal multiplier = multiplierFor(rr.getRoom().getRoomType());
            BigDecimal effectiveRate = baseRate.multiply(multiplier);

            BigDecimal lineTotal = effectiveRate
                    .multiply(BigDecimal.valueOf(nights))
                    .setScale(2, RoundingMode.HALF_UP);

            rr.setLineTotal(lineTotal);
            total = total.add(lineTotal);
        }

        reservation.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal multiplierFor(RoomType type) {
        return switch (type) {
            case STANDARD -> new BigDecimal("1.00");
            case DELUXE -> new BigDecimal("1.10");
            case FAMILY_SUITE -> new BigDecimal("1.20");
        };
    }

    @Override
    public String getStrategyName() {
        return "RoomTypeSurchargeBillingStrategy";
    }
}
