package com.oceanview.service;

import com.oceanview.dao.FakeRoomDAO;
import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.UpdateRoomDTO;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.exception.ResourceNotFoundException;
import com.oceanview.model.Room;
import com.oceanview.model.enums.RoomType;
import com.oceanview.service.impl.RoomServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class RoomServiceImplTest {
    private FakeRoomDAO fakeDAO;
    private RoomServiceImpl service;

    @Before
    public void setUp() {
        fakeDAO = new FakeRoomDAO();
        service = new RoomServiceImpl(fakeDAO);
    }

    //get all rooms test
    @Test
    public void getAllRooms_shouldReturnAllSeededRooms() {
        fakeDAO.seed(new Room("A", "d", new BigDecimal("10000"), RoomType.STANDARD, true));
        fakeDAO.seed(new Room("B", "d", new BigDecimal("15000"), RoomType.DELUXE, true));

        Assert.assertEquals(2, service.getAllRooms().size());
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

        Assert.assertNotNull(service.createRoom(dto));
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

        Assert.assertNotNull(service.getRoomById(r.getRoomId()));
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

        Assert.assertEquals(
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

        Assert.assertTrue(fakeDAO.getRoomById(r.getRoomId()).isAvailable());
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

}
