package com.oceanview.service.billing;

import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

public class TestBillingStrategy implements BillingStrategy{
    @Override
    public void applyPricing(Reservation reservation) {

        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }
        if (reservation.getReservationRooms() == null) {
            reservation.setTotalAmount(BigDecimal.ZERO);
            return;
        }
        long nights = ChronoUnit.DAYS.between(
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        if (nights <= 0) {
            nights = 1;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ReservationRoom rr : reservation.getReservationRooms()) {

            BigDecimal rate = rr.getRatePerNight() != null ? rr.getRatePerNight() : BigDecimal.ZERO;

            BigDecimal lineTotal = rate.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
            rr.setLineTotal(lineTotal);
            total = total.add(lineTotal);
        }
        reservation.setTotalAmount(
                total.setScale(2, RoundingMode.HALF_UP)
        );
    }

    @Override
    public String getStrategyName() {
        return "TestBillingStrategy";
    }
}
