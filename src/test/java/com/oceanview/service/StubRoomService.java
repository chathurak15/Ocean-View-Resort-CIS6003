package com.oceanview.service;

import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.dto.room.UpdateRoomDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StubRoomService implements RoomService{
    private final Map<Integer, RoomDTO> store = new HashMap<>();

    public void seed(RoomDTO r) { store.put(r.getRoomId(), r); }

    @Override
    public RoomDTO createRoom(CreateRoomDTO dto) {
        return null;
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        return List.of();
    }

    @Override
    public RoomDTO getRoomById(int id) { return store.get(id); }

    @Override
    public RoomDTO updateRoomDetails(int id, UpdateRoomDTO dto) {
        return null;
    }

    @Override
    public void updateRoomStatus(int id, boolean available) {

    }

    @Override
    public void deleteRoom(int id) {

    }
}
