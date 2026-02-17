package com.oceanview.dao;

import com.oceanview.model.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeRoomDAO implements RoomDAO {
    private final Map<Integer, Room> store = new HashMap<>();
    private int idSeq = 1;

    public void seed(Room room) {
        if (room.getRoomId() == null) {
            room.assignId(idSeq++);
        }
        store.put(room.getRoomId(), room);
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
        if (!store.containsKey(room.getRoomId())) {
            return null; // service should check existence before calling update
        }
        store.put(room.getRoomId(), room);
        return room;
    }

    @Override
    public void updateRoomStatus(int roomId, boolean available) {
        Room room = store.get(roomId);
        if (room == null) return;

        // just change status like DB update
        store.put(roomId, room);
    }

    @Override
    public void deleteRoom(int roomId) {
        store.remove(roomId);
    }

    @Override
    public boolean existsByName(String roomName) {
        if (roomName == null) return false;
        String normalized = roomName.trim().toLowerCase();
        for (Room r : store.values()) {
            if (r.getRoomName().trim().toLowerCase().equals(normalized)) {
                return true;
            }
        }
        return false;
    }
}
