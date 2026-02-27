package com.oceanview.dao;

import com.oceanview.model.Guest;
import com.oceanview.service.search.GuestSearchCriteria;

import java.util.*;
import java.util.stream.Collectors;

public class FakeGuestDAO implements GuestDAO{
    private final Map<Integer, Guest> store = new HashMap<>();
    private int idSeq = 1;

    public void seed(Guest guest) {
        if (guest.getGuestId() == null) {
            guest.setGuestId(idSeq++);
        }
        store.put(guest.getGuestId(), guest);
    }

    @Override
    public List<Guest> getAllGuests() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Guest addGuest(Guest guest) {
        guest.setGuestId(idSeq++);
        store.put(guest.getGuestId(), guest);
        return guest;
    }

    @Override
    public List<Guest> search(GuestSearchCriteria criteria) {
        if (criteria == null) return getAllGuests();

        return store.values().stream()
                .filter(g -> matches(criteria, g))
                .collect(Collectors.toList());
    }

    private boolean matches(GuestSearchCriteria c, Guest g) {
        if (c.getGuestId() != null) {
            return Objects.equals(g.getGuestId(), c.getGuestId());
        }
        if (c.getNic() != null && !c.getNic().isBlank()) {
            return c.getNic().trim().equalsIgnoreCase(nullSafe(g.getNic()));
        }
        if (c.getPhoneNumber() != null && !c.getPhoneNumber().isBlank()) {
            return c.getPhoneNumber().trim().equals(nullSafe(g.getPhoneNumber()));
        }
        return true;
    }

    @Override
    public boolean existsByNic(String nic) {
        if (nic == null) return false;
        String normalized = nic.trim().toLowerCase();

        return store.values().stream()
                .anyMatch(g -> nullSafe(g.getNic()).trim().toLowerCase().equals(normalized));
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return false;
        String normalized = phoneNumber.trim();

        return store.values().stream()
                .anyMatch(g -> nullSafe(g.getPhoneNumber()).trim().equals(normalized));
    }

    @Override
    public Guest getByGuestId(int id) {
        return store.get(id);
    }

    @Override
    public Guest updateGuest(Guest guest) {
        if (guest == null || guest.getGuestId() == null) return null;
        if (!store.containsKey(guest.getGuestId())) return null;

        store.put(guest.getGuestId(), guest);
        return guest;
    }

    @Override
    public void deleteGuest(int id) {
        store.remove(id);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
