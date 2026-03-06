package com.oceanview.service.impl;

import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.impl.GuestDAOImpl;
import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.exception.ResourceNotFoundException;
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

    //update guest
    @Override
    public GuestDTO updateGuest(int id, CreateGuestDTO dto) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid guest id");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Guest data is required");
        }
        // Retrieve existing guest
        Guest existingGuest = guestDAO.getByGuestId(id);
        if (existingGuest == null) {
            throw new ResourceNotFoundException("Guest not found with id: " + id);
        }
        // Validate NIC uniqueness (excluding current guest)
        if (!existingGuest.getNic().equals(dto.getNic()) &&
                guestDAO.existsByNic(dto.getNic())) {
            throw new DuplicateResourceException("NIC already exists");
        }
        // Validate Phone uniqueness (excluding current guest)
        if (!existingGuest.getPhoneNumber().equals(dto.getPhoneNumber()) &&
                guestDAO.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists");
        }
        existingGuest.setName(dto.getName());
        existingGuest.setEmail(dto.getEmail());
        existingGuest.setPhoneNumber(dto.getPhoneNumber());
        existingGuest.setAddress(dto.getAddress());
        existingGuest.setNic(dto.getNic());

        Guest updatedGuest = guestDAO.updateGuest(existingGuest);
        return GuestMapper.toDTO(updatedGuest);
    }

    @Override
    public void deleteGuest(int id) {
        if (id <= 0) throw new IllegalArgumentException("Invalid guest id");
        Guest existingGuest = guestDAO.getByGuestId(id);
        if (existingGuest == null) throw new ResourceNotFoundException("Guest not found");
        guestDAO.deleteGuest(id);
    }

    @Override
    public GuestDTO getGuestById(int id) {
        if (id <= 0) throw new IllegalArgumentException("Invalid guest id");
        Guest guest = guestDAO.getByGuestId(id);
        if (guest == null) throw new ResourceNotFoundException("Guest not found");
        return GuestMapper.toDTO(guest);
    }

}
