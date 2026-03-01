package com.oceanview.model;

import java.math.BigDecimal;

public class ReservationRoom {
    private Integer reservationRoomId;
    private Reservation reservation;
    private Room room;
    private BigDecimal ratePerNight;
    private BigDecimal lineTotal;

    public Integer getReservationRoomId() {
        return reservationRoomId;
    }

    public void setReservationRoomId(Integer reservationRoomId) {
        this.reservationRoomId = reservationRoomId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public BigDecimal getRatePerNight() {
        return ratePerNight;
    }

    public void setRatePerNight(BigDecimal ratePerNight) {
        this.ratePerNight = ratePerNight;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
