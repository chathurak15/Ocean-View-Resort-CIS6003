package com.oceanview.service;

import com.oceanview.dao.FakeRoomDAO;
import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.dto.room.UpdateRoomDTO;
import com.oceanview.exception.BusinessRuleException;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.exception.ResourceNotFoundException;
import com.oceanview.model.Room;
import com.oceanview.model.enums.RoomType;
import com.oceanview.model.enums.Status;
import com.oceanview.service.impl.RoomServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class RoomServiceImplTest {
    private FakeRoomDAO fakeDAO;
    private RoomServiceImpl service;

    @Before
    public void setUp() {
        fakeDAO = new FakeRoomDAO();
        service = new RoomServiceImpl(fakeDAO);

        fakeDAO.seed(new Room("R1", "d", new BigDecimal("10000"), RoomType.STANDARD, true));
        fakeDAO.seed(new Room("R2", "d", new BigDecimal("12000"), RoomType.DELUXE, true));
        fakeDAO.seed(new Room("R3", "d", new BigDecimal("15000"), RoomType.FAMILY_SUITE, false));
    }

    //get all rooms test
    @Test
    public void getAllRooms_shouldReturnAllSeededRooms() {
        assertEquals(3, service.getAllRooms().size());
    }

    //create room valid test
    @Test
    public void createRoom_shouldCreate_whenValid() {
        CreateRoomDTO dto = new CreateRoomDTO();
        dto.setRoomName("Deluxe 01");
        dto.setRoomDescription("Sea view");
        dto.setRoomPrice(new BigDecimal("25000"));
        dto.setRoomType(RoomType.DELUXE);
        dto.setAvailable(true);

        assertNotNull(service.createRoom(dto));
    }
    //duplicate room name test
    @Test(expected = DuplicateResourceException.class)
    public void createRoom_shouldThrow_whenDuplicateName() {
        Room existing = new Room(
                "Deluxe 01",
                "Sea view",
                new BigDecimal("20000"),
                RoomType.DELUXE,
                true
        );
        fakeDAO.seed(existing);
        CreateRoomDTO dto = new CreateRoomDTO();
        dto.setRoomName("Deluxe 01");
        dto.setRoomDescription("Sea view");
        dto.setRoomPrice(new BigDecimal("25000"));
        dto.setRoomType(RoomType.DELUXE);
        dto.setAvailable(true);

        service.createRoom(dto);
    }

    //get room by id test
    @Test
    public void getRoomById_shouldReturnRoom_whenExists() {
        Room r = new Room("Std 01", "Basic", new BigDecimal("10000"), RoomType.STANDARD, true);
        fakeDAO.seed(r);

        assertNotNull(service.getRoomById(r.getRoomId()));
    }
    //get room by id test(NotFound exception)
    @Test(expected = ResourceNotFoundException.class)
    public void getRoomById_shouldThrow_whenNotFound() {
        service.getRoomById(999);
    }

    //update room details test
    @Test
    public void updateRoomDetails_shouldUpdate_whenValid() {
        Room existing = new Room("Std 01", "Basic", new BigDecimal("10000"), RoomType.STANDARD, true);
        fakeDAO.seed(existing);

        UpdateRoomDTO dto = new UpdateRoomDTO();
        dto.setRoomName("Std 01 Updated");
        dto.setRoomDescription("Updated desc");
        dto.setRoomPrice(new BigDecimal("12000"));
        dto.setRoomType(RoomType.STANDARD);

        assertEquals(
                "Std 01 Updated", service.updateRoomDetails(existing.getRoomId(), dto).getRoomName()
        );
    }
    //update room details test(NotFound exception)
    @Test(expected = ResourceNotFoundException.class)
    public void updateRoomDetails_shouldThrow_whenRoomNotFound() {
        UpdateRoomDTO dto = new UpdateRoomDTO();
        dto.setRoomName("X");
        dto.setRoomDescription("d");
        dto.setRoomPrice(new BigDecimal("10000"));
        dto.setRoomType(RoomType.STANDARD);

        service.updateRoomDetails(999, dto);
    }
    //duplicate room name update test
    @Test(expected = DuplicateResourceException.class)
    public void updateRoomDetails_shouldThrow_whenNameBecomesDuplicate() {
        Room r1 = new Room("Room A", "A", new BigDecimal("10000"), RoomType.STANDARD, true);
        Room r2 = new Room("Room B", "B", new BigDecimal("15000"), RoomType.DELUXE, true);
        fakeDAO.seed(r1);
        fakeDAO.seed(r2);

        UpdateRoomDTO dto = new UpdateRoomDTO();
        dto.setRoomName("Room A"); // duplicate of r1
        dto.setRoomDescription("New");
        dto.setRoomPrice(new BigDecimal("20000"));
        dto.setRoomType(RoomType.DELUXE);

        service.updateRoomDetails(r2.getRoomId(), dto);
    }

    //update room status test
    @Test
    public void updateRoomStatus_shouldBookRoom_whenAvailableFalse() {
        Room r = new Room("Std 02", "Basic", new BigDecimal("9000"), RoomType.STANDARD, true);
        fakeDAO.seed(r);

        service.updateRoomStatus(r.getRoomId(), false);

        Room after = fakeDAO.getRoomById(r.getRoomId());
        Assert.assertFalse(after.isAvailable());
    }
    //update room status test(NotFound exception)
    @Test(expected = ResourceNotFoundException.class)
    public void updateRoomStatus_shouldThrow_whenRoomNotFound() {
        service.updateRoomStatus(999, false);
    }
    //update room status test(Already available)
    @Test
    public void updateRoomStatus_shouldKeepAvailable_whenAlreadyAvailable() {
        Room r = new Room("Std 10", "Basic", new BigDecimal("9000"), RoomType.STANDARD, true);
        fakeDAO.seed(r);

        service.updateRoomStatus(r.getRoomId(), true);

        assertTrue(fakeDAO.getRoomById(r.getRoomId()).isAvailable());
    }

    //delete room test
    @Test
    public void deleteRoom_shouldDelete_whenExists() {
        Room r = new Room("Std 03", "Basic", new BigDecimal("9000"), RoomType.STANDARD, true);
        fakeDAO.seed(r);
        service.deleteRoom(r.getRoomId());

        Assert.assertNull(fakeDAO.getRoomById(r.getRoomId()));
    }
    //delete room test(NotFound exception)
    @Test(expected = ResourceNotFoundException.class)
    public void deleteRoom_shouldThrow_whenNotFound() {
        service.deleteRoom(999);
    }

    @Test(expected = BusinessRuleException.class)
    public void getAvailableRooms_nullCheckIn_shouldThrow() {
        service.getAvailableRooms(null, LocalDate.now().plusDays(2));
    }

    @Test(expected = BusinessRuleException.class)
    public void getAvailableRooms_nullCheckOut_shouldThrow() {
        service.getAvailableRooms(LocalDate.now().plusDays(1), null);
    }

    @Test(expected = BusinessRuleException.class)
    public void getAvailableRooms_invalidRange_sameDay_shouldThrow() {
        LocalDate d = LocalDate.of(2026, 3, 10);
        service.getAvailableRooms(d, d);
    }

    @Test(expected = BusinessRuleException.class)
    public void getAvailableRooms_invalidRange_checkInAfterCheckOut_shouldThrow() {
        LocalDate in = LocalDate.of(2026, 3, 12);
        LocalDate out = LocalDate.of(2026, 3, 10);
        service.getAvailableRooms(in, out);
    }

    @Test
    public void getAvailableRooms_shouldExcludeMaintenanceRooms() {
        LocalDate in = LocalDate.of(2026, 3, 10);
        LocalDate out = LocalDate.of(2026, 3, 12);

        List<RoomDTO> available = service.getAvailableRooms(in, out);

        // R3 is maintenance => excluded
        assertEquals(2, available.size());
        assertTrue(available.stream().noneMatch(r -> r.getRoomName().equals("R3")));
    }

    @Test
    public void getAvailableRooms_shouldExcludeOverlappingConfirmedReservations() {
        // Create a CONFIRMED reservation that overlaps for room 1 (R1)
        fakeDAO.seedReservation(
                1,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 15),
                Status.CONFIRMED
        );

        LocalDate in = LocalDate.of(2026, 3, 12);
        LocalDate out = LocalDate.of(2026, 3, 14);

        List<RoomDTO> available = service.getAvailableRooms(in, out);

        // R1 should be blocked, R2 should be available, R3 maintenance excluded
        assertEquals(1, available.size());
        assertEquals("R2", available.get(0).getRoomName());
    }

    @Test
    public void getAvailableRooms_shouldNotExcludeCancelledReservations() {
        // Cancelled reservation should NOT block availability
        fakeDAO.seedReservation(
                2,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 15),
                Status.CANCELLED
        );

        LocalDate in = LocalDate.of(2026, 3, 12);
        LocalDate out = LocalDate.of(2026, 3, 14);

        List<RoomDTO> available = service.getAvailableRooms(in, out);

        // R2 should still be available
        assertTrue(available.stream().anyMatch(r -> r.getRoomName().equals("R2")));
    }

    @Test
    public void getAvailableRooms_boundaryCase_checkInEqualsExistingCheckOut_shouldBeAvailable() {
        // existing booking ends on 15th
        fakeDAO.seedReservation(
                1,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 15),
                Status.CONFIRMED
        );

        // new booking starts exactly on existing checkOut => NOT overlap
        LocalDate in = LocalDate.of(2026, 3, 15);
        LocalDate out = LocalDate.of(2026, 3, 17);

        List<RoomDTO> available = service.getAvailableRooms(in, out);

        // R1 should be available due to boundary rule
        assertTrue(available.stream().anyMatch(r -> r.getRoomName().equals("R1")));
    }

    @Test
    public void getAvailableRooms_shouldReturnRoomDTOsWithIds() {
        LocalDate in = LocalDate.of(2026, 3, 10);
        LocalDate out = LocalDate.of(2026, 3, 12);

        List<RoomDTO> available = service.getAvailableRooms(in, out);

        assertFalse(available.isEmpty());
        assertNotNull(available.get(0).getRoomId());
    }
}

