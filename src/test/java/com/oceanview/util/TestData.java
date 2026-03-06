package com.oceanview.util;

import com.oceanview.model.Guest;
import com.oceanview.model.Reservation;
import com.oceanview.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestData {
    public static Reservation reservation(String no, LocalDateTime createdAt) {
        Reservation r = new Reservation();
        r.setReservationNo(no);
        r.setStatus(Status.CONFIRMED);
        r.setCheckInDate(LocalDate.of(2026,3,10));
        r.setCheckOutDate(LocalDate.of(2026,3,12));
        r.setCreatedAt(createdAt);
        r.setTotalAmount(new BigDecimal("1000.00"));
        Guest g = new Guest("G", "e@e.com", "077", "addr", "nic");
        g.setGuestId(1);
        r.setGuest(g);
        r.setReservationRooms(new ArrayList<>());
        return r;
    }
}
