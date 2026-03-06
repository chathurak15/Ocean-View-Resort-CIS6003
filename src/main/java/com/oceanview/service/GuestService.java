package com.oceanview.service;

import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.service.search.GuestSearchCriteria;

import java.util.List;

public interface GuestService {
    List<GuestDTO> getAllGuests();
    GuestDTO addGuest(CreateGuestDTO dto);
    List<GuestDTO> searchGuests(GuestSearchCriteria criteria);
    GuestDTO updateGuest(int id, CreateGuestDTO dto);
    void deleteGuest(int id);
    GuestDTO getGuestById(int id);
}
