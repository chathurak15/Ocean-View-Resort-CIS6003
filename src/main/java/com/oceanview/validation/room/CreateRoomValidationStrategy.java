package com.oceanview.validation.room;

import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.validation.ValidationStrategy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class CreateRoomValidationStrategy implements ValidationStrategy<CreateRoomDTO> {
    @Override
    public Map<String, String> validate(CreateRoomDTO dto) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (dto == null) {
            errors.put("request", "Request body is missing or invalid JSON.");
            return errors;
        }

        // roomName
        String name = dto.getRoomName();
        if (name == null || name.trim().isEmpty()) {
            errors.put("roomName", "Room name is required.");
        } else if (name.trim().length() < 3) {
            errors.put("roomName", "Room name must be at least 3 characters.");
        } else if (name.length() > 80) {
            errors.put("roomName", "Room name cannot exceed 80 characters.");
        }

        // roomPrice
        BigDecimal price = dto.getRoomPrice();
        if (price == null) {
            errors.put("roomPrice", "Room price is required.");
        } else if (price.compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("roomPrice", "Room price must be greater than zero.");
        }

        // roomType
        if (dto.getRoomType() == null) {
            errors.put("roomType", "Room type must be selected.");
        }

        // roomDescription
        String desc = dto.getRoomDescription();
        if (desc != null && desc.length() > 500) {
            errors.put("roomDescription", "Room description cannot exceed 500 characters.");
        }

        return errors;
    }
}
