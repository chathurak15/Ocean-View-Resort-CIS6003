package com.oceanview.controller;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.impl.ReservationDAOImpl;
import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.dto.Reservation.ReservationDTO;
import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.facade.ReservationFacade;
import com.oceanview.facade.impl.ReservationFacadeImpl;
import com.oceanview.service.GuestService;
import com.oceanview.service.RoomService;
import com.oceanview.service.billing.BillingStrategy;
import com.oceanview.service.billing.RoomTypeSurchargeBillingStrategy;
import com.oceanview.service.impl.GuestServiceImpl;
import com.oceanview.service.impl.RoomServiceImpl;
import com.oceanview.validation.ValidatorContext;
import com.oceanview.validation.reservation.CreateReservationValidationStrategy;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ReservationServlet", urlPatterns = {"/api/reservations/*"})
public class ReservationController extends BaseServlet{
    private final ValidatorContext validatorContext = new ValidatorContext();
    private ReservationFacade reservationFacade;

    @Override
    public void init() {
        validatorContext.register(CreateReservationDTO.class, new CreateReservationValidationStrategy());

        GuestService guestService = new GuestServiceImpl();
        RoomService roomService = new RoomServiceImpl();
        ReservationDAO reservationDAO = new ReservationDAOImpl();
        BillingStrategy billingStrategy = new RoomTypeSurchargeBillingStrategy();
        reservationFacade = new ReservationFacadeImpl(guestService, roomService, reservationDAO, billingStrategy);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                List<ReservationDTO> allReservation = reservationFacade.getAllReservations();
                sendJsonResponse(resp, allReservation);
                return;
            }
            // GET /api/reservation/{reservationNumber}
            String reservationNo = pathInfo.substring(1);
            ReservationDTO reservationDTO = reservationFacade.getByReservationNo(reservationNo);
            sendJsonResponse(resp, reservationDTO);

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && !"/".equals(pathInfo)) {
                sendErrorResponse(resp, 404, "Endpoint not found");
                return;
            }
            CreateReservationDTO dto = mapper.readValue(req.getReader(), CreateReservationDTO.class);

            Map<String, String> errors = validatorContext.validate(dto, CreateReservationDTO.class);
            if (!errors.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("status", "VALIDATION_ERROR", "errors", errors));
                return;
            }
            ReservationDTO reservationDTO = reservationFacade.createReservation(dto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            sendJsonResponse(resp, reservationDTO);

        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
