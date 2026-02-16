package com.oceanview.model;

import com.oceanview.model.enums.RoomType;

import java.math.BigDecimal;

public class Room {
    private Integer roomId;
    private String roomName;
    private String roomDescription;
    private BigDecimal roomPrice;
    private RoomType roomType;
    private boolean available;

    public Room(String roomName, String roomDescription,BigDecimal roomPrice, RoomType roomType, boolean available) {
        this.setRoomName(roomName);
        this.setRoomDescription(roomDescription);
        this.setRoomPrice(roomPrice);
        this.setRoomType(roomType);
        this.available = available;
    }
    public int getRoomId() {
        return roomId;
    }

    public void assignId(Integer roomId) {
        if (this.roomId != null) {
            throw new IllegalStateException("Room ID already assigned.");
        }
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        if (roomName == null || roomName.isBlank()) {
            throw new IllegalArgumentException("Room name cannot be empty.");
        }
        this.roomName = roomName;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public BigDecimal getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(BigDecimal roomPrice) {
        if (roomPrice == null || roomPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Room price must be positive.");
        }
        this.roomPrice = roomPrice;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("Room type must be specified.");
        }
        this.roomType = roomType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void markAsBooked() {
        if (!this.available) {
            throw new IllegalStateException("Room is already booked.");
        }
        this.available = false;
    }

    public void markAsAvailable() {
        if (this.available) {
            throw new IllegalStateException("Room is already available.");
        }
        this.available = true;
    }
}
