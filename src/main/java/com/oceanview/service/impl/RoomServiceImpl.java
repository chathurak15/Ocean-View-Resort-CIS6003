package com.oceanview.service.impl;

import com.oceanview.dao.RoomDAO;
import com.oceanview.dao.impl.RoomDAOImpl;
import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.dto.room.UpdateRoomDTO;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.exception.ResourceNotFoundException;
import com.oceanview.mapper.RoomMapper;
import com.oceanview.model.Room;
import com.oceanview.service.RoomService;

import java.util.List;
import java.util.stream.Collectors;

public class RoomServiceImpl implements RoomService {
    private final RoomDAO roomDAO;

    public RoomServiceImpl() {
        this.roomDAO = new RoomDAOImpl();
    }

    @Override
    public RoomDTO createRoom(CreateRoomDTO dto) {
        // Room name must be unique
        String name = dto.getRoomName().trim();
        if (roomDAO.existsByName(name)) {
            throw new DuplicateResourceException("Room name already exists.");
        }
        Room room = RoomMapper.toEntity(dto);
        Room savedRoom = roomDAO.createRoom(room);
        return RoomMapper.toDTO(savedRoom);
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        List<Room> roomList = roomDAO.getAllRooms();
        return roomList.stream().map(RoomMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public RoomDTO getRoomById(int id) {
        Room room = roomDAO.getRoomById(id);
        if (room == null) throw new ResourceNotFoundException("Room not found: " + id);
        return RoomMapper.toDTO(room);
    }

    @Override
    public RoomDTO updateRoomDetails(int id, UpdateRoomDTO dto) {
        Room existing = roomDAO.getRoomById(id);
        if (existing == null) throw new ResourceNotFoundException("Room not found: " + id);

        String newName = dto.getRoomName() == null ? null : dto.getRoomName().trim();
        // if name changed, check duplicates
        if (newName != null && !newName.equals(existing.getRoomName())
                && roomDAO.existsByName(newName)) {
            throw new DuplicateResourceException("Room name already exists.");
        }

        // Build updated entity (keep availability)
        Room updated = new Room(
                newName,
                dto.getRoomDescription(),
                dto.getRoomPrice(),
                dto.getRoomType(),
                existing.isAvailable()
        );
        updated.assignId(id);

        roomDAO.updateRoom(updated);
        return RoomMapper.toDTO(updated);
    }

    @Override
    public void updateRoomStatus(int id, boolean available) {
        Room room = roomDAO.getRoomById(id);
        if (room == null) throw new ResourceNotFoundException("Room not found: " + id);

        // domain behavior
        if (available) room.markAsAvailable();
        else room.markAsBooked();

        roomDAO.updateRoomStatus(id, available);
    }

    @Override
    public void deleteRoom(int id) {
        Room room = roomDAO.getRoomById(id);
        if (room == null) throw new ResourceNotFoundException("Room not found: " + id);

        roomDAO.deleteRoom(id);
    }
}
