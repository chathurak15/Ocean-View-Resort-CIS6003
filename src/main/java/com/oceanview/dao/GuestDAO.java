package com.oceanview.dao;

import com.oceanview.model.Guest;
import com.oceanview.service.search.GuestSearchCriteria;
import java.util.List;

public interface GuestDAO {
    List<Guest> getAllGuests();
    Guest addGuest(Guest guest);
    List<Guest> search(GuestSearchCriteria criteria);
    boolean existsByNic(String nic);
    boolean existsByPhoneNumber(String phoneNumber);
    Guest getByGuestId(int id);
    Guest updateGuest(Guest guest);
    void deleteGuest(int id);
}
