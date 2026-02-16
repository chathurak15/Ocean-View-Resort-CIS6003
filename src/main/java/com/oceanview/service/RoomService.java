package com.oceanview.service;

import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;

import java.util.List;

public interface RoomService {
    RoomDTO createRoom(CreateRoomDTO dto);

    List<RoomDTO> getAllRooms();
}
