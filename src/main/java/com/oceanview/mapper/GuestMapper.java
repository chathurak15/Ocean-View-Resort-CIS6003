package com.oceanview.mapper;

import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.model.Guest;

public class GuestMapper {
    private GuestMapper() {}

    //guest to guestDTO
    public static GuestDTO toDTO(Guest guest) {
        GuestDTO guestDTO = new GuestDTO();
        guestDTO.setGuestId(guest.getGuestId());
        guestDTO.setName(guest.getName());
        guestDTO.setEmail(guest.getEmail());
        guestDTO.setPhoneNumber(guest.getPhoneNumber());
        guestDTO.setAddress(guest.getAddress());
        guestDTO.setNic(guest.getNic());
        guestDTO.setCreatedAt(guest.getCreatedAt());
        return guestDTO;
    }

    public static Guest toEntity(CreateGuestDTO dto) {
        Guest guest = new Guest(
                dto.getName(),
                dto.getEmail(),
                dto.getPhoneNumber(),
                dto.getAddress(),
                dto.getNic());
        return guest;
    }
}
