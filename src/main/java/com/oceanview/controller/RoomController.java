package com.oceanview.controller;

import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.exception.DataAccessException;
import com.oceanview.exception.DuplicateResourceException;
import com.oceanview.service.RoomService;
import com.oceanview.service.impl.RoomServiceImpl;
import com.oceanview.validation.ValidatorContext;
import com.oceanview.validation.room.CreateRoomValidationStrategy;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "RoomServlet", urlPatterns = {"/api/rooms/*"})
public class RoomController extends BaseServlet {
    private final RoomService roomService = new RoomServiceImpl();

    //get all rooms
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            List<RoomDTO> allRooms = roomService.getAllRooms();
            sendJsonResponse(resp, allRooms);
        } else {
            sendErrorResponse(resp, 404, "Endpoint not found");
        }
    }

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

            // Validate using Strategy Pattern
            ValidatorContext ctx = new ValidatorContext();
            ctx.register(CreateRoomDTO.class, new CreateRoomValidationStrategy());

            Map<String, String> errors = ctx.validate(dto, CreateRoomDTO.class);
            if (!errors.isEmpty()) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("status", "VALIDATION_ERROR");
                payload.put("errors", errors);

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, payload);
                return;
            }
            // Call service (business layer)
            try {
                RoomDTO created = roomService.createRoom(dto);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                sendJsonResponse(resp, created);
            } catch (DuplicateResourceException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
            } catch (DataAccessException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
            } catch (Exception e) {
                sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
            }
            return;
        }
        sendErrorResponse(resp, 404, "Endpoint not found");
    }

}
