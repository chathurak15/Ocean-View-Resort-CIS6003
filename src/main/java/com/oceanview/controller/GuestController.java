package com.oceanview.controller;

import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.service.GuestService;
import com.oceanview.service.impl.GuestServiceImpl;
import com.oceanview.service.search.GuestSearchCriteria;
import com.oceanview.validation.ValidatorContext;
import com.oceanview.validation.guest.CreateGuestValidationStrategy;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "GuestServlet", urlPatterns = { "/api/guests/*" })
public class GuestController extends BaseServlet {
    private GuestService guestService;
    private final ValidatorContext validatorContext = new ValidatorContext();

    public GuestController() {
        this.guestService = new GuestServiceImpl();
    }

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    public void init() {
        validatorContext.register(CreateGuestDTO.class, new CreateGuestValidationStrategy());
    }

    // get all guests & search guests
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                List<GuestDTO> allGuests = guestService.getAllGuests();
                sendJsonResponse(resp, allGuests);
                return;
            }
            // search endpoint - /guests/search?nic=... OR ?phone=... OR ?id=...
            if (pathInfo != null && pathInfo.startsWith("/search")) {
                handleSearch(req, resp);
                return;
            }
            int id = Integer.parseInt(pathInfo.substring(1));
            GuestDTO guestDTO = guestService.getGuestById(id);
            sendJsonResponse(resp, guestDTO);

        } catch (NumberFormatException e) {
            handleException(resp, new IllegalArgumentException("Invalid guest id in URL", e));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    // add new guest
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && !"/".equals(pathInfo)) {
                sendErrorResponse(resp, 404, "Endpoint not found");
                return;
            }
            CreateGuestDTO dto = mapper.readValue(req.getReader(), CreateGuestDTO.class);

            Map<String, String> errors = validatorContext.validate(dto, CreateGuestDTO.class);
            if (!errors.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("status", "VALIDATION_ERROR", "errors", errors));
                return;
            }
            GuestDTO created = guestService.addGuest(dto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            sendJsonResponse(resp, created);

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    // update guest
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || "/".equals(pathInfo)) {
                sendErrorResponse(resp, 400, "Guest ID is required");
                return;
            }
            int id = Integer.parseInt(pathInfo.substring(1));
            CreateGuestDTO dto = mapper.readValue(req.getReader(), CreateGuestDTO.class);
            Map<String, String> errors = validatorContext.validate(dto, CreateGuestDTO.class);
            if (!errors.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("status", "VALIDATION_ERROR", "errors", errors));
                return;
            }
            GuestDTO updated = guestService.updateGuest(id, dto);
            sendJsonResponse(resp, updated);
        } catch (Exception e) {
            handleException(resp, e);
        }

    }

    // delete guest
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || "/".equals(pathInfo)) {
                sendErrorResponse(resp, 400, "Guest ID is required");
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));
            guestService.deleteGuest(id);
            sendJsonResponse(resp, Map.of("status", "DELETED"));

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    // handle search guests method (search guest by id, nic, or phone one at a time)
    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String nicParam = req.getParameter("nic");
        String phoneParam = req.getParameter("phone");

        // allow exactly ONE search field at a time ()
        int provided = (idParam != null && !idParam.isBlank() ? 1 : 0) +
                (nicParam != null && !nicParam.isBlank() ? 1 : 0) +
                (phoneParam != null && !phoneParam.isBlank() ? 1 : 0);

        if (provided != 1) {
            throw new IllegalArgumentException("Provide exactly one parameter: id OR nic OR phone");
        }

        GuestSearchCriteria criteria = new GuestSearchCriteria();

        if (idParam != null && !idParam.isBlank()) {
            criteria.setGuestId(Integer.parseInt(idParam));
        } else if (nicParam != null && !nicParam.isBlank()) {
            criteria.setNic(nicParam.trim());
        } else {
            criteria.setPhoneNumber(phoneParam.trim());
        }

        List<GuestDTO> results = guestService.searchGuests(criteria);
        sendJsonResponse(resp, results);
    }
}
