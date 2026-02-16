package com.oceanview.dao;

import com.oceanview.model.Room;

import java.util.List;

public interface RoomDAO {
    List<Room> getAllRooms();

    Room createRoom(Room room);
}
