package com.oceanview.service;

import com.oceanview.dao.FakeGuestDAO;
import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.exception.ResourceNotFoundException;
import com.oceanview.model.Guest;
import com.oceanview.service.impl.GuestServiceImpl;
import com.oceanview.service.search.GuestSearchCriteria;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GuestServiceImplTest {
    private FakeGuestDAO fakeDAO;
    private GuestServiceImpl service;

    @Before
    public void setUp() {
        fakeDAO = new FakeGuestDAO();
        service = new GuestServiceImpl(fakeDAO);
    }

    private CreateGuestDTO dto(String name, String email, String phone, String address, String nic) {
        CreateGuestDTO d = new CreateGuestDTO();
        d.setName(name);
        d.setEmail(email);
        d.setPhoneNumber(phone);
        d.setAddress(address);
        d.setNic(nic);
        return d;
    }

    private Guest seedGuest(String name, String email, String phone, String address, String nic) {
        Guest g = new Guest(name, email, phone, address, nic);
        fakeDAO.seed(g);
        return g;
    }

    //ADD
    @Test
    public  void addGuest_success_whenUniqueNicAndPhone() {
        GuestDTO created = service.addGuest(dto("A", "a@mail.com", "0771234567", "Galle", "NIC001"));

        assertNotNull(created);
        assertNotNull(created.getGuestId());
        assertEquals("NIC001", created.getNic());
        assertEquals("0771234567", created.getPhoneNumber());
    }
    @Test
    public void addGuest_throwsDuplicate_whenNicExists() {
        seedGuest("X", "x@mail.com", "0770000000", "Addr", "NIC001");

        assertThrows(DuplicateResourceException.class,
                () -> service.addGuest(dto("A", "a@mail.com", "0771234567", "Galle", "NIC001")));
    }
    @Test
    public void addGuest_throwsDuplicate_whenPhoneExists() {
        seedGuest("X", "x@mail.com", "0771234567", "Addr", "NIC999");

        assertThrows(DuplicateResourceException.class, () -> service.addGuest(dto
                ("A", "a@mail.com", "0771234567", "Galle", "NIC001")));
    }

    // GET BY ID
    @Test
    public void getGuestById_success() {
        Guest seeded = seedGuest("A", "a@mail.com", "0771234567", "Galle", "NIC001");

        GuestDTO found = service.getGuestById(seeded.getGuestId());

        assertEquals(seeded.getGuestId(), found.getGuestId());
        assertEquals("NIC001", found.getNic());
    }
    @Test
    public void getGuestById_throws_whenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.getGuestById(999));
    }
    @Test
    public void getGuestById_throws_whenInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> service.getGuestById(0));
        assertThrows(IllegalArgumentException.class, () -> service.getGuestById(-1));
    }

    // GET ALL
    @Test
    public void getAllGuests_returnsAllGuests() {
        seedGuest("A", "a@mail.com", "0771111111", "Galle", "NIC001");
        seedGuest("B", "b@mail.com", "0772222222", "Colombo", "NIC002");

        var guests = service.getAllGuests();
        assertEquals(2, guests.size());
    }

    //SEARCH
    @Test
    public void searchGuests_byId_returnsCorrectGuest() {
        Guest seeded = seedGuest("A", "a@mail.com", "0771111111", "Galle", "NIC001");
        var criteria = new GuestSearchCriteria();
        criteria.setGuestId(seeded.getGuestId());
        var result = service.searchGuests(criteria);
        assertEquals(1, result.size());
        assertEquals("NIC001", result.get(0).getNic());
    }
    @Test
    public void searchGuests_returnsEmpty_whenNoMatch() {
        var criteria = new GuestSearchCriteria();
        criteria.setNic("UNKNOWN");

        var result = service.searchGuests(criteria);

        assertTrue(result.isEmpty());
    }

    // UPDATE
    @Test
    public void updateGuest_success_whenSameNicAndPhone() {
        Guest seeded = seedGuest("Old", "old@mail.com", "0771234567", "OldAddr", "NIC001");

        GuestDTO updated = service.updateGuest(
                seeded.getGuestId(),
                dto("New", "new@mail.com", "0771234567", "NewAddr", "NIC001")
        );
        assertEquals(seeded.getGuestId(), updated.getGuestId());
        assertEquals("New", updated.getName());
        assertEquals("NewAddr", updated.getAddress());
    }
    @Test
    public void updateGuest_throwsDuplicate_whenChangingNicToExisting() {
        Guest g1 = seedGuest("G1", "g1@mail.com", "0771111111", "A", "NIC001");
        seedGuest("G2", "g2@mail.com", "0772222222", "B", "NIC002");

        assertThrows(DuplicateResourceException.class, () -> service.updateGuest(g1.getGuestId(),
                        dto("G1", "g1@mail.com", "0771111111", "A", "NIC002")));
    }
    @Test
    public void updateGuest_throwsDuplicate_whenChangingPhoneToExisting() {
        Guest g1 = seedGuest("G1", "g1@mail.com", "0771111111", "A", "NIC001");
        seedGuest("G2", "g2@mail.com", "0772222222", "B", "NIC002");

        assertThrows(DuplicateResourceException.class, () -> service.updateGuest(g1.getGuestId(),
                        dto("G1", "g1@mail.com", "0772222222", "A", "NIC001")));
    }
    @Test
    public void updateGuest_throws_whenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.updateGuest(
                999, dto("A", "a@mail.com", "0771234567", "Galle", "NIC001")));
    }
    @Test
    public void updateGuest_throws_whenInvalidIdOrNullDto() {
        assertThrows(IllegalArgumentException.class, () -> service.updateGuest(
                0, dto("A", "a@mail.com", "0771234567", "Galle", "NIC001")));
        assertThrows(IllegalArgumentException.class, () -> service.updateGuest(
                -5, dto("A", "a@mail.com", "0771234567", "Galle", "NIC001")));
        assertThrows(IllegalArgumentException.class, () -> service.updateGuest(
                1, null));
    }

    //DELETE
    @Test
    public void deleteGuest_success() {
        Guest seeded = seedGuest("A", "a@mail.com", "0771234567", "Galle", "NIC001");
        service.deleteGuest(seeded.getGuestId());
        assertThrows(ResourceNotFoundException.class, () -> service.getGuestById(seeded.getGuestId()));
    }
    @Test
    public void deleteGuest_throws_whenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.deleteGuest(999));
    }
    @Test
    public void deleteGuest_throws_whenInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteGuest(0));
        assertThrows(IllegalArgumentException.class, () -> service.deleteGuest(-2));
    }
}
