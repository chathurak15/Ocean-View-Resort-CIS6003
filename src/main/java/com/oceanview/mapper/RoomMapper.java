package com.oceanview.mapper;

import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.model.Room;

public class RoomMapper {

    private RoomMapper() {}

    public static RoomDTO toDTO(Room room) {
        if (room == null) {
            return null;
        }
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomId(room.getRoomId());
        roomDTO.setRoomName(room.getRoomName());
        roomDTO.setRoomDescription(room.getRoomDescription());
        roomDTO.setRoomPrice(room.getRoomPrice());
        roomDTO.setRoomType(room.getRoomType());
        roomDTO.setAvailable(room.isAvailable());

        return roomDTO;
    }

    // Create DTO → Entity
    public static Room toEntity(CreateRoomDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateRoomDTO cannot be null");
        }

        return new Room(
                dto.getRoomName(),
                dto.getRoomDescription(),
                dto.getRoomPrice(),
                dto.getRoomType(),
                dto.isAvailable()
        );
    }
}
