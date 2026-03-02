package com.oceanview.service;

import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.service.search.GuestSearchCriteria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StubGuestService implements GuestService{
    private final Map<Integer, GuestDTO> store = new HashMap<>();

    public void seed(GuestDTO g) { store.put(g.getGuestId(), g); }

    @Override
    public GuestDTO getGuestById(int id) { return store.get(id); }

    @Override
    public List<GuestDTO> getAllGuests() {
        return List.of();
    }

    @Override
    public GuestDTO addGuest(CreateGuestDTO dto) {
        return null;
    }

    @Override
    public List<GuestDTO> searchGuests(GuestSearchCriteria criteria) {
        return List.of();
    }

    @Override
    public GuestDTO updateGuest(int id, CreateGuestDTO dto) {
        return null;
    }

    @Override
    public void deleteGuest(int id) {

    }
}
