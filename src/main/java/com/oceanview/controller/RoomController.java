package com.oceanview.controller;

import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.service.RoomService;
import com.oceanview.service.impl.RoomServiceImpl;
import com.oceanview.validation.ValidatorContext;
import com.oceanview.validation.room.CreateRoomValidationStrategy;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "RoomServlet", urlPatterns = {"/api/rooms/*"})
public class RoomController extends BaseServlet {
    private final RoomService roomService = new RoomServiceImpl();
    private final ValidatorContext validatorContext = new ValidatorContext();

    @Override
    public void init() {
        validatorContext.register(CreateRoomDTO.class, new CreateRoomValidationStrategy());
    }

    //get all rooms
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            try {
                List<RoomDTO> allRooms = roomService.getAllRooms();
                sendJsonResponse(resp, allRooms);
            } catch (Exception e) {
                handleException(resp, e);
            }
            return;
        }
        sendErrorResponse(resp, 404, "Endpoint not found");
    }

    //create room
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            CreateRoomDTO dto;
            try {
                dto = mapper.readValue(req.getReader(), CreateRoomDTO.class);
            } catch (Exception ex) {
                sendErrorResponse(resp, 400, "Invalid JSON request body");
                return;
            }

            Map<String, String> errors = validatorContext.validate(dto, CreateRoomDTO.class);
            if (!errors.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("status", "VALIDATION_ERROR", "errors", errors));
                return;
            }

            try {
                RoomDTO created = roomService.createRoom(dto);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                sendJsonResponse(resp, created);
            } catch (Exception e) {
                handleException(resp, e);
            }
            return;
        }
        sendErrorResponse(resp, 404, "Endpoint not found");
    }

}
