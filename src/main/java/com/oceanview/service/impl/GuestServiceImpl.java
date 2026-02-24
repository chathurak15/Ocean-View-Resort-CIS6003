package com.oceanview.service.impl;

import com.oceanview.dao.GuestDAO;
import com.oceanview.dao.impl.GuestDAOImpl;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.mapper.GuestMapper;
import com.oceanview.model.Guest;
import com.oceanview.service.GuestService;

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
}
