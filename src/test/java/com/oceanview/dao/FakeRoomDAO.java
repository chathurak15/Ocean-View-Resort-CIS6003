package com.oceanview.dao;

import com.oceanview.model.Room;
import com.oceanview.model.enums.Status;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FakeRoomDAO implements RoomDAO {

    private final List<ReservationWindow> reservations = new ArrayList<>();
    private final Map<Integer, Room> store = new HashMap<>();
    private int idSeq = 1;

    // Seed room for tests
    public void seed(Room room) {
        if (room.getRoomId() == null) {
            room.assignId(idSeq++);
        }
        store.put(room.getRoomId(), room);
    }

    // Seed reservation window for tests
    public void seedReservation(int roomId, LocalDate checkIn, LocalDate checkOut, Status status) {
        reservations.add(new ReservationWindow(roomId, checkIn, checkOut, status));
    }

    private boolean overlaps(LocalDate newIn, LocalDate newOut, LocalDate in, LocalDate out) {
        return newIn.isBefore(out) && newOut.isAfter(in);
    }

    @Override
    public Room createRoom(Room room) {
        room.assignId(idSeq++);
        store.put(room.getRoomId(), room);
        return room;
    }

    @Override
    public List<Room> getAllRooms() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Room getRoomById(int roomId) {
        return store.get(roomId);
    }

    @Override
    public Room updateRoom(Room room) {
        if (!store.containsKey(room.getRoomId())) return null;
        store.put(room.getRoomId(), room);
        return room;
    }

    @Override
    public void updateRoomStatus(int roomId, boolean available) {
        Room room = store.get(roomId);
        if (room == null) return;

        // IMPORTANT: actually change the state
        if (available) {
            if (!room.isAvailable()) room.markOutOfService();
        } else {
            if (room.isAvailable()) room.markInService();
        }
    }

    @Override
    public void deleteRoom(int roomId) {
        store.remove(roomId);
    }

    @Override
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {

        // only in-service rooms (available = true)
        List<Room> inServiceRooms = store.values()
                .stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());

        // remove rooms with overlapping CONFIRMED reservations
        return inServiceRooms.stream()
                .filter(room -> reservations.stream().noneMatch(rw ->
                        rw.roomId == room.getRoomId()
                                && rw.status == Status.CONFIRMED
                                && overlaps(checkIn, checkOut, rw.checkIn, rw.checkOut)
                ))
                .sorted(Comparator.comparing(Room::getRoomId))
                .collect(Collectors.toList());
    }

    private static class ReservationWindow {
        final int roomId;
        final LocalDate checkIn;
        final LocalDate checkOut;
        final Status status;

        ReservationWindow(int roomId, LocalDate checkIn, LocalDate checkOut, Status status) {
            this.roomId = roomId;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.status = status;
        }
    }

    @Override
    public boolean existsByName(String roomName) {
        if (roomName == null) return false;
        String normalized = roomName.trim().toLowerCase();
        for (Room r : store.values()) {
            if (r.getRoomName().trim().toLowerCase().equals(normalized)) return true;
        }
        return false;
    }
}