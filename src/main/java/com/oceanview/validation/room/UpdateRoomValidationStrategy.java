package com.oceanview.validation.room;

import com.oceanview.dto.room.UpdateRoomDTO;
import com.oceanview.validation.ValidationStrategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class UpdateRoomValidationStrategy implements ValidationStrategy<UpdateRoomDTO> {
    @Override
    public Map<String, String> validate(UpdateRoomDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto.getRoomName() == null || dto.getRoomName().isBlank()) {
            errors.put("roomName", "Room name is required.");
        }

        if (dto.getRoomPrice() == null || dto.getRoomPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("roomPrice", "Room price must be positive.");
        }

        if (dto.getRoomType() == null) {
            errors.put("roomType", "Room type is required.");
        }

        return errors;
    }
}
