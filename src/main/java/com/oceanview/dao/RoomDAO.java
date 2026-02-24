package com.oceanview.dao;

import com.oceanview.model.Room;

import java.util.List;

public interface RoomDAO {
    List<Room> getAllRooms();

    Room createRoom(Room room);

    boolean existsByName(String roomName);

    Room getRoomById(int roomId);

    Room updateRoom(Room room);

    void updateRoomStatus(int roomId, boolean available);

    void deleteRoom(int roomId);
}
