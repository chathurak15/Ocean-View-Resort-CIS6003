package com.oceanview.service.billing;

import com.oceanview.exception.BusinessRuleException;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

public class FlatRateBillingStrategy implements BillingStrategy{
    @Override
    public void applyPricing(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }

        long nights = ChronoUnit.DAYS.between(
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        if (nights <= 0) {
            throw new BusinessRuleException("Invalid date range. Nights must be positive.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ReservationRoom rr : reservation.getReservationRooms()) {
            BigDecimal rate = rr.getRatePerNight();
            if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Invalid room rate.");
            }

            BigDecimal lineTotal = rate
                    .multiply(BigDecimal.valueOf(nights))
                    .setScale(2, RoundingMode.HALF_UP);

            rr.setLineTotal(lineTotal);

            totalAmount = totalAmount.add(lineTotal);
        }

        reservation.setTotalAmount(
                totalAmount.setScale(2, RoundingMode.HALF_UP)
        );
    }

    @Override
    public String getStrategyName() {
        return "FlatRateBillingStrategy";
    }
}
