package com.oceanview.validation.guest;

import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.validation.ValidationStrategy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CreateGuestValidationStrategy implements ValidationStrategy<CreateGuestDTO> {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");


    @Override
    public Map<String, String> validate(CreateGuestDTO dto) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (dto == null) {
            errors.put("request", "Request body is missing or invalid JSON.");
            return errors;
        }

        // Name
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            errors.put("name", "Guest name is required.");
        } else if (dto.getName().trim().length() < 3) {
            errors.put("name", "Guest name must be at least 3 characters.");
        }

        // Email
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            errors.put("email", "Email is required.");
        } else if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            errors.put("email", "Invalid email format.");
        }

        // Phone number
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
            errors.put("phoneNumber", "Phone number is required.");
        } else if (dto.getPhoneNumber().length() < 10) {
            errors.put("phoneNumber", "Phone number must be at least 10 digits.");
        }

        // NIC
        if (dto.getNic() == null || dto.getNic().trim().isEmpty()) {
            errors.put("nic", "NIC is required.");
        }
        return errors;
    }

}
