package com.oceanview.service.billing;

import com.oceanview.exception.BusinessRuleException;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FlatRateBillingStrategyTest {
    private BillingStrategy strategy;

    @Before
    public void setUp() {
        strategy = new FlatRateBillingStrategy();
    }

    private Reservation buildReservation(LocalDate in, LocalDate out, List<ReservationRoom> rooms) {
        Reservation r = new Reservation();
        r.setCheckInDate(in);
        r.setCheckOutDate(out);
        r.setReservationRooms(rooms);
        return r;
    }

    private ReservationRoom rr(BigDecimal ratePerNight) {
        ReservationRoom rr = new ReservationRoom();
        rr.setRatePerNight(ratePerNight);
        return rr;
    }

    @Test
    public void applyPricing_singleRoom_shouldCalculateTotals() {
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 15),
                new ArrayList<>(List.of(rr(new BigDecimal("10000.00"))))
        );
        strategy.applyPricing(reservation);

        assertNotNull(reservation.getTotalAmount());
        assertEquals(new BigDecimal("50000.00"), reservation.getTotalAmount());
        assertEquals(new BigDecimal("50000.00"), reservation.getReservationRooms().get(0).getLineTotal());
    }

    @Test
    public void applyPricing_multipleRooms_shouldSumTotals() {
        // 2 nights: (10000*2) + (15000*2) = 50000
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                new ArrayList<>(List.of(
                        rr(new BigDecimal("10000.00")),
                        rr(new BigDecimal("15000.00"))
                ))
        );

        strategy.applyPricing(reservation);
        assertEquals(new BigDecimal("20000.00"), reservation.getReservationRooms().get(0).getLineTotal());
        assertEquals(new BigDecimal("30000.00"), reservation.getReservationRooms().get(1).getLineTotal());
        assertEquals(new BigDecimal("50000.00"), reservation.getTotalAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyPricing_nullReservation_shouldThrow() {
        strategy.applyPricing(null);
    }

    @Test(expected = BusinessRuleException.class)
    public void applyPricing_invalidDateRange_shouldThrow() {
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10),
                new ArrayList<>(List.of(rr(new BigDecimal("10000.00"))))
        );
        strategy.applyPricing(reservation);
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyPricing_invalidRoomRate_shouldThrow() {
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                new ArrayList<>(List.of(rr(new BigDecimal("0.00"))))
        );
        strategy.applyPricing(reservation);
    }

    @Test
    public void getStrategyName_shouldReturnName() {
        assertEquals("FlatRateBillingStrategy", strategy.getStrategyName());
    }
}
