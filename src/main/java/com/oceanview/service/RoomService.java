package com.oceanview.service;

import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.dto.room.UpdateRoomDTO;

import java.util.List;

public interface RoomService {
    RoomDTO createRoom(CreateRoomDTO dto);

    List<RoomDTO> getAllRooms();

    RoomDTO getRoomById(int id);

    RoomDTO updateRoomDetails(int id, UpdateRoomDTO dto);

    void updateRoomStatus(int id, boolean available);

    void deleteRoom(int id);
}
