package com.oceanview.service.impl;

import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.impl.GuestDAOImpl;
import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.mapper.GuestMapper;
import com.oceanview.model.Guest;
import com.oceanview.service.GuestService;
import com.oceanview.service.search.GuestSearchCriteria;

import java.util.List;
import java.util.stream.Collectors;

public class GuestServiceImpl implements GuestService {
    private final GuestDAO guestDAO;

    public GuestServiceImpl() {
        this.guestDAO = new GuestDAOImpl();
    }
    public GuestServiceImpl(GuestDAO guestDAO) {
        this.guestDAO = guestDAO;
    }

    //get all guests
    @Override
    public List<GuestDTO> getAllGuests() {
        List<Guest> guestList = guestDAO.getAllGuests();
        return guestList.stream().map(GuestMapper::toDTO).collect(Collectors.toList());
    }

    //add guest
    @Override
    public GuestDTO addGuest(CreateGuestDTO dto) {
        Guest guest = GuestMapper.toEntity(dto);
        if (guestDAO.existsByNic(guest.getNic())) {
            throw new DuplicateResourceException("Guest with NIC already exists");
        }
        if (guestDAO.existsByPhoneNumber(guest.getPhoneNumber())) {
            throw new DuplicateResourceException("Guest with phone number already exists");
        }
        Guest savedGuest = guestDAO.addGuest(guest);
        return GuestMapper.toDTO(savedGuest);
    }

    //search guests
    @Override
    public List<GuestDTO> searchGuests(GuestSearchCriteria criteria) {
        List<Guest> guestList = guestDAO.search(criteria);
        return guestList.stream().map(GuestMapper::toDTO).toList();
    }

}
