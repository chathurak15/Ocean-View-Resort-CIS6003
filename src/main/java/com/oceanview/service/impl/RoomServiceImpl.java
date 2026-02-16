package com.oceanview.service.impl;

import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.impl.RoomDAOImpl;
import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.mapper.RoomMapper;
import com.oceanview.model.Room;
import com.oceanview.service.RoomService;

import java.util.List;
import java.util.stream.Collectors;

public class RoomServiceImpl implements RoomService {
    private final RoomDAO roomDAO = new RoomDAOImpl();
    @Override
    public RoomDTO createRoom(CreateRoomDTO dto) {
         Room room = roomDAO.createRoom(RoomMapper.toEntity(dto));
         return RoomMapper.toDTO(room);
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        List<Room> roomList = roomDAO.getAllRooms();
        return roomList.stream().map(RoomMapper::toDTO).collect(Collectors.toList());
    }
}
