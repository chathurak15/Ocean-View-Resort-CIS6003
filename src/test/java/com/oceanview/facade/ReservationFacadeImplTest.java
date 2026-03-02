package com.oceanview.facade;

import com.oceanview.dao.FakeReservationDAO;
import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.dto.Reservation.ReservationDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.exception.BusinessRuleException;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.exception.ResourceNotFoundException;
import com.oceanview.facade.impl.ReservationFacadeImpl;
import com.oceanview.model.Reservation;
import com.oceanview.model.enums.RoomType;
import com.oceanview.model.enums.Status;
import com.oceanview.service.StubGuestService;
import com.oceanview.service.StubRoomService;
import com.oceanview.service.billing.BillingStrategy;
import com.oceanview.service.billing.TestBillingStrategy;
import com.oceanview.util.TestData;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class ReservationFacadeImplTest {
    FakeReservationDAO reservationDAO;
    private StubGuestService guestService;
    private StubRoomService roomService;
    private ReservationFacadeImpl reservationFacade;

    @Before
    public void setUp() {
        reservationDAO = new FakeReservationDAO();
        guestService = new StubGuestService();
        roomService = new StubRoomService();

        BillingStrategy billingStrategy = new TestBillingStrategy();
        reservationFacade = new ReservationFacadeImpl(
                guestService,
                roomService,
                reservationDAO,
                billingStrategy
        );
        // Seed Guest
        GuestDTO guest = new GuestDTO();
        guest.setGuestId(1);
        guest.setName("Test Guest");
        guestService.seed(guest);

        // Seed Room
        RoomDTO room = new RoomDTO();
        room.setRoomId(101);
        room.setRoomName("Standard-101");
        room.setRoomPrice(new BigDecimal("10000"));
        room.setRoomType(RoomType.STANDARD);
        room.setAvailable(true);
        roomService.seed(room);
    }

    // SUCCESSFUL RESERVATION
    @Test
    public void createReservation_success() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 15));
        dto.setRoomIds(List.of(101));
        ReservationDTO result = reservationFacade.createReservation(dto);

        assertNotNull(result);
        assertTrue(result.getReservationNo().startsWith("RES-"));
        assertEquals(Status.CONFIRMED, result.getStatus());
        assertEquals(0, result.getTotalAmount().compareTo(new BigDecimal("50000")));
    }
    // INVALID DATE RANGE
    @Test(expected = IllegalArgumentException.class)
    public void createReservation_invalidDateRange_shouldThrow() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 15));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 10));
        dto.setRoomIds(List.of(101));

        reservationFacade.createReservation(dto);
    }
    //GUEST NOT FOUND
    @Test(expected = ResourceNotFoundException.class)
    public void createReservation_guestNotFound_shouldThrow() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(999);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(101));

        reservationFacade.createReservation(dto);
    }
    //ROOM NOT FOUND
    @Test(expected = ResourceNotFoundException.class)
    public void createReservation_roomNotFound_shouldThrow() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(999));

        reservationFacade.createReservation(dto);
    }
    // ROOM OUT OF SERVICE
    @Test(expected = BusinessRuleException.class)
    public void createReservation_roomOutOfService_shouldThrow() {
        RoomDTO room = roomService.getRoomById(101);
        room.setAvailable(false);
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(101));
        reservationFacade.createReservation(dto);
    }
    // OVERLAPPING RESERVATION
    @Test(expected = DuplicateResourceException.class)
    public void createReservation_overlap_shouldThrow() {
        // First reservation
        CreateReservationDTO first = new CreateReservationDTO();
        first.setGuestId(1);
        first.setCheckInDate(LocalDate.of(2026, 3, 10));
        first.setCheckOutDate(LocalDate.of(2026, 3, 12));
        first.setRoomIds(List.of(101));

        reservationFacade.createReservation(first);

        // Overlapping reservation
        CreateReservationDTO second = new CreateReservationDTO();
        second.setGuestId(1);
        second.setCheckInDate(LocalDate.of(2026, 3, 11));
        second.setCheckOutDate(LocalDate.of(2026, 3, 13));
        second.setRoomIds(List.of(101));

        reservationFacade.createReservation(second);
    }

    //GET ALL RESERVATIONS EMPTY
    @Test
    public void getAllReservations_whenEmpty_shouldReturnEmptyList() {
        List<ReservationDTO> list = reservationFacade.getAllReservations();

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
    //GET ALL RESERVATIONS
    @Test
    public void getAllReservations_afterCreate_shouldReturnOne() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(101));
        reservationFacade.createReservation(dto);
        List<ReservationDTO> list = reservationFacade.getAllReservations();
        assertEquals(1, list.size());
        assertEquals(Status.CONFIRMED, list.get(0).getStatus());
    }

    //GET RESERVATION BY RESERVATION NO
    @Test
    public void getByReservationNo_success() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(101));
        ReservationDTO saved = reservationFacade.createReservation(dto);

        ReservationDTO fetched = reservationFacade.getByReservationNo(saved.getReservationNo());

        assertNotNull(fetched);
        assertEquals(saved.getReservationNo(), fetched.getReservationNo());
        assertEquals(Status.CONFIRMED, fetched.getStatus());
    }
    //GET RESERVATION BY RESERVATION NO NOT FOUND
    @Test(expected = ResourceNotFoundException.class)
    public void getByReservationNo_notFound_shouldThrow() {
        reservationFacade.getByReservationNo("RES-12345");
    }
    //GET RESERVATION BY RESERVATION NO NULL
    @Test(expected = BusinessRuleException.class)
    public void getByReservationNo_null_shouldThrow() {
        reservationFacade.getByReservationNo(null);
    }
    //GET RESERVATION BY RESERVATION NO BLANK
    @Test(expected = BusinessRuleException.class)
    public void getByReservationNo_blank_shouldThrow() {
        reservationFacade.getByReservationNo("   ");
    }

    //CANCEL RESERVATION
    @Test
    public void cancelReservation_success() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(101));

        ReservationDTO saved = reservationFacade.createReservation(dto);
        reservationFacade.cancelReservation(saved.getReservationNo());
        ReservationDTO updated = reservationFacade.getByReservationNo(saved.getReservationNo());
        assertEquals(Status.CANCELLED, updated.getStatus());
    }
    //CANCEL RESERVATION ALREADY CANCELLED
    @Test(expected = BusinessRuleException.class)
    public void cancelReservation_alreadyCancelled_shouldThrow() {

        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(101));

        ReservationDTO saved = reservationFacade.createReservation(dto);

        reservationFacade.cancelReservation(saved.getReservationNo());

        // second cancel → should throw
        reservationFacade.cancelReservation(saved.getReservationNo());
    }
    @Test(expected = BusinessRuleException.class)
    public void cancelReservation_null_shouldThrow() {
        reservationFacade.cancelReservation(null);
    }
    @Test(expected = BusinessRuleException.class)
    public void cancelReservation_blank_shouldThrow() {
        reservationFacade.cancelReservation(" ");
    }

    //COMPLETE RESERVATION
    @Test
    public void completeReservation_success() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(101));

        ReservationDTO saved = reservationFacade.createReservation(dto);
        reservationFacade.completeReservation(saved.getReservationNo());
        ReservationDTO updated = reservationFacade.getByReservationNo(saved.getReservationNo());
        assertEquals(Status.COMPLETED, updated.getStatus());
    }
    @Test(expected = BusinessRuleException.class)
    public void completeReservation_alreadyCompleted_shouldThrow() {
        CreateReservationDTO dto = new CreateReservationDTO();
        dto.setGuestId(1);
        dto.setCheckInDate(LocalDate.of(2026, 3, 10));
        dto.setCheckOutDate(LocalDate.of(2026, 3, 12));
        dto.setRoomIds(List.of(101));
        ReservationDTO saved = reservationFacade.createReservation(dto);
        reservationFacade.completeReservation(saved.getReservationNo());

        reservationFacade.completeReservation(saved.getReservationNo());
    }
    @Test(expected = BusinessRuleException.class)
    public void completeReservation_null_shouldThrow() {
        reservationFacade.completeReservation(null);
    }
    @Test(expected = BusinessRuleException.class)
    public void completeReservation_blank_shouldThrow() {
        reservationFacade.completeReservation(" ");
    }

    @Test(expected = BusinessRuleException.class)
    public void getReservationsByDateRange_shouldThrow_whenFromMissing() {
        reservationFacade.getReservationsByDateRange(null, "2026-03-10");
    }
    @Test(expected = BusinessRuleException.class)
    public void getReservationsByDateRange_shouldThrow_whenInvalidFormat() {
        reservationFacade.getReservationsByDateRange("03-01-2026", "2026-03-10");
    }
    @Test(expected = BusinessRuleException.class)
    public void getReservationsByDateRange_shouldThrow_whenFromAfterTo() {
        reservationFacade.getReservationsByDateRange("2026-03-11", "2026-03-10");
    }
    @Test
    public void getReservationsByDateRange_shouldReturnOnlyInRange() {
        // seed 3 reservations with createdAt
        Reservation r1 = TestData.reservation("RES-AAA11111", LocalDateTime.of(2026,3,1,10,0));
        Reservation r2 = TestData.reservation("RES-BBB22222", LocalDateTime.of(2026,3,5,10,0));
        Reservation r3 = TestData.reservation("RES-CCC33333", LocalDateTime.of(2026,3,20,10,0));
        reservationDAO.seed(r1);
        reservationDAO.seed(r2);
        reservationDAO.seed(r3);
        List<ReservationDTO> results = reservationFacade.getReservationsByDateRange("2026-03-01", "2026-03-10");

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(x -> "RES-AAA11111".equals(x.getReservationNo())));
        assertTrue(results.stream().anyMatch(x -> "RES-BBB22222".equals(x.getReservationNo())));
        assertFalse(results.stream().anyMatch(x -> "RES-CCC33333".equals(x.getReservationNo())));
    }
    @Test
    public void getReservationsByDateRange_shouldIncludeBoundaries() {
        Reservation r1 = TestData.reservation("RES-BOUND1", LocalDateTime.of(2026,3,1,0,0));
        Reservation r2 = TestData.reservation("RES-BOUND2", LocalDateTime.of(2026,3,10,23,0));
        reservationDAO.seed(r1);
        reservationDAO.seed(r2);

        List<ReservationDTO> results = reservationFacade.getReservationsByDateRange("2026-03-01", "2026-03-10");
        assertEquals(2, results.size());
    }
}
