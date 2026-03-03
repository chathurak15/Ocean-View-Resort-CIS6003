package com.oceanview.controller;

import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.impl.ReservationDAOImpl;
import com.oceanview.dto.Reservation.CreateReservationDTO;
import com.oceanview.dto.Reservation.ReservationDTO;
import com.oceanview.facade.ReservationFacade;
import com.oceanview.facade.impl.ReservationFacadeImpl;
import com.oceanview.service.GuestService;
import com.oceanview.service.RoomService;
import com.oceanview.service.billing.BillingStrategy;
import com.oceanview.service.billing.BillingStrategyFactory;
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

@WebServlet(name = "ReservationServlet", urlPatterns = { "/api/reservations/*" })
public class ReservationController extends BaseServlet {
    private final ValidatorContext validatorContext = new ValidatorContext();
    private ReservationFacade reservationFacade;

    @Override
    public void init() {
        validatorContext.register(CreateReservationDTO.class, new CreateReservationValidationStrategy());

        GuestService guestService = new GuestServiceImpl();
        RoomService roomService = new RoomServiceImpl();
        ReservationDAO reservationDAO = new ReservationDAOImpl();
        BillingStrategy billingStrategy = BillingStrategyFactory.create(BillingStrategyFactory.BillingType.SURCHARGE);
        reservationFacade = new ReservationFacadeImpl(guestService, roomService, reservationDAO, billingStrategy);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                String fromStr = req.getParameter("from");
                String toStr = req.getParameter("to");

                // Date range mode(use for genarate report)
                if (fromStr != null || toStr != null) {
                    if (fromStr == null || toStr == null) {
                        sendErrorResponse(resp, 400, "Both 'from' and 'to' are required");
                        return;
                    }
                    ReservationDTO[] results = reservationFacade.getReservationsByDateRange(fromStr, toStr)
                            .toArray(new ReservationDTO[0]);

                    sendJsonResponse(resp, results);
                    return;
                }
                // Normal mode
                List<ReservationDTO> all = reservationFacade.getAllReservations();
                sendJsonResponse(resp, all);
                return;
            }
            String reservationNo = pathInfo.substring(1).trim();
            if (reservationNo.isBlank()) {
                sendErrorResponse(resp, 400, "Reservation number is required");
                return;
            }
            ReservationDTO reservationDTO = reservationFacade.getByReservationNo(reservationNo);
            sendJsonResponse(resp, reservationDTO);
            return;

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

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(resp, 400, "Reservation number is required");
                return;
            }

            // /api/reservations/{reservationNo}/cancel OR /complete
            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length == 2) {
                String reservationNo = parts[0].trim();
                String action = parts[1].trim().toLowerCase();

                if (reservationNo.isBlank()) {
                    sendErrorResponse(resp, 400, "Reservation number is required");
                    return;
                }

                if (action.equals("cancel")) {
                    reservationFacade.cancelReservation(reservationNo);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    sendJsonResponse(resp, Map.of("status", "CANCELLED"));
                    return;
                }

                if (action.equals("complete")) {
                    reservationFacade.completeReservation(reservationNo);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    sendJsonResponse(resp, Map.of("status", "COMPLETED"));
                    return;
                }
            }

            sendErrorResponse(resp, 404, "Endpoint not found");

        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
