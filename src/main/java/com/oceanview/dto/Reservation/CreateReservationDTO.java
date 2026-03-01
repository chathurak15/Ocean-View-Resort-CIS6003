package com.oceanview.dto.Reservation;
import com.oceanview.model.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CreateReservationDTO {
    private Integer guestId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Status status;
    private List<Integer> roomIds;

    public Integer getGuestId() {
        return guestId;
    }

    public void setGuestId(Integer guestId) {
        this.guestId = guestId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Integer> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<Integer> roomIds) {
        this.roomIds = roomIds;
    }
}
