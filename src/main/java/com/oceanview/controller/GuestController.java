package com.oceanview.controller;

import com.oceanview.dto.guest.CreateGuestDTO;
import com.oceanview.dto.guest.GuestDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.service.GuestService;
import com.oceanview.service.impl.GuestServiceImpl;
import com.oceanview.validation.ValidatorContext;
import com.oceanview.validation.guest.CreateGuestValidationStrategy;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "RoomServlet", urlPatterns = {"/api/guests/*"})
public class GuestController extends BaseServlet{
    private final GuestService guestService = new GuestServiceImpl();
    private final ValidatorContext validatorContext = new ValidatorContext();

    public void init() {
        validatorContext.register(CreateGuestDTO.class, new CreateGuestValidationStrategy());
    }

    //get all Guests
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                List<GuestDTO> allGuests = guestService.getAllGuests();
                sendJsonResponse(resp, allGuests);
                return;
            }

        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
