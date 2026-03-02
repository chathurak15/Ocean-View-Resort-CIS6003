package com.oceanview.service.billing;

import com.oceanview.exception.BusinessRuleException;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationRoom;
import com.oceanview.model.Room;
import com.oceanview.model.enums.RoomType;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RoomTypeSurchargeBillingStrategyTest {

    private BillingStrategy strategy;

    @Before
    public void setUp() {
        strategy = new RoomTypeSurchargeBillingStrategy();
    }

    private Reservation buildReservation(LocalDate in, LocalDate out, List<ReservationRoom> rooms) {
        Reservation r = new Reservation();
        r.setCheckInDate(in);
        r.setCheckOutDate(out);
        r.setReservationRooms(rooms);
        return r;
    }

    private ReservationRoom rr(RoomType type, BigDecimal baseRate) {
        Room room = new Room("R1", "Test", baseRate, type, true); // available doesn’t matter for billing
        room.assignId(1);

        ReservationRoom rr = new ReservationRoom();
        rr.setRoom(room);
        rr.setRatePerNight(baseRate);
        return rr;
    }

    @Test
    public void applyPricing_standard_shouldApply1_00Multiplier() {
        // 2 nights 10000 * 1.00 * 2 = 20000
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                new ArrayList<>(List.of(rr(RoomType.STANDARD, new BigDecimal("10000.00"))))
        );
        strategy.applyPricing(reservation);
        assertEquals(new BigDecimal("20000.00"), reservation.getTotalAmount());
        assertEquals(new BigDecimal("20000.00"), reservation.getReservationRooms().get(0).getLineTotal());
    }

    @Test
    public void applyPricing_deluxe_shouldApply1_10Multiplier() {
        // 2 nights 10000 * 1.10 * 2 = 22000
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                new ArrayList<>(List.of(rr(RoomType.DELUXE, new BigDecimal("10000.00"))))
        );
        strategy.applyPricing(reservation);
        assertEquals(new BigDecimal("22000.00"), reservation.getTotalAmount());
    }

    @Test
    public void applyPricing_familySuite_shouldApply1_20Multiplier() {
        // 5 nights 10000 * 1.20 * 5 = 60000
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 15),
                new ArrayList<>(List.of(rr(RoomType.FAMILY_SUITE, new BigDecimal("10000.00"))))
        );
        strategy.applyPricing(reservation);
        assertEquals(new BigDecimal("60000.00"), reservation.getTotalAmount());
    }

    @Test(expected = BusinessRuleException.class)
    public void applyPricing_nullReservation_shouldThrow() {
        strategy.applyPricing(null);
    }

    @Test(expected = BusinessRuleException.class)
    public void applyPricing_noRooms_shouldThrow() {
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                new ArrayList<>()
        );
        strategy.applyPricing(reservation);
    }

    @Test(expected = BusinessRuleException.class)
    public void applyPricing_invalidDateRange_shouldThrow() {
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 10),
                new ArrayList<>(List.of(rr(RoomType.STANDARD, new BigDecimal("10000.00"))))
        );
        strategy.applyPricing(reservation);
    }

    @Test(expected = IllegalArgumentException.class)
    public void applyPricing_missingRoomType_shouldThrow() {
        Room room = new Room("R1", "Test", new BigDecimal("10000.00"), RoomType.STANDARD, true);
        room.assignId(1);
        ReservationRoom rr = new ReservationRoom();
        rr.setRoom(room);
        // Force roomType to null (simulate bad mapping)
        rr.getRoom().setRoomType(null);
    }

    @Test(expected = BusinessRuleException.class)
    public void applyPricing_roomNull_shouldThrow() {
        ReservationRoom rr = new ReservationRoom();
        rr.setRoom(null);
        rr.setRatePerNight(new BigDecimal("10000.00"));
        Reservation reservation = buildReservation(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                new ArrayList<>(List.of(rr))
        );
        strategy.applyPricing(reservation);
    }

    @Test(expected = BusinessRuleException.class)
    public void applyPricing_invalidRate_shouldThrow() {
        // valid room object (positive price)
        Room room = new Room("R1", "Test", new BigDecimal("10000.00"), RoomType.STANDARD, true);
        room.assignId(1);
        ReservationRoom rr = new ReservationRoom();
        rr.setRoom(room);
        rr.setRatePerNight(new BigDecimal("0.00"));
        Reservation reservation = new Reservation();
        reservation.setCheckInDate(LocalDate.of(2026, 3, 10));
        reservation.setCheckOutDate(LocalDate.of(2026, 3, 12));
        reservation.setReservationRooms(new ArrayList<>(List.of(rr)));
        strategy.applyPricing(reservation);
    }

    @Test
    public void getStrategyName_shouldReturnName() {
        assertEquals("RoomTypeSurchargeBillingStrategy", strategy.getStrategyName());
    }
}
