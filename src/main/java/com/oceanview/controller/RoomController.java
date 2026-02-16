package com.oceanview.controller;

import com.oceanview.dto.room.CreateRoomDTO;
import com.oceanview.dto.room.RoomDTO;
import com.oceanview.dto.room.RoomStatusDTO;
import com.oceanview.dto.room.UpdateRoomDTO;
import com.oceanview.service.RoomService;
import com.oceanview.service.impl.RoomServiceImpl;
import com.oceanview.validation.ValidatorContext;
import com.oceanview.validation.room.CreateRoomValidationStrategy;
import com.oceanview.validation.room.UpdateRoomValidationStrategy;

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
        validatorContext.register(UpdateRoomDTO.class, new UpdateRoomValidationStrategy());
    }

    //get all rooms
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                List<RoomDTO> allRooms = roomService.getAllRooms();
                sendJsonResponse(resp, allRooms);
                return;
            }
            // GET /api/rooms/{id}
            int id = Integer.parseInt(pathInfo.substring(1));
            RoomDTO room = roomService.getRoomById(id);
            sendJsonResponse(resp, room);

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    //create room
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && !"/".equals(pathInfo)) {
                sendErrorResponse(resp, 404, "Endpoint not found");
                return;
            }
            CreateRoomDTO dto = mapper.readValue(req.getReader(), CreateRoomDTO.class);

            Map<String, String> errors = validatorContext.validate(dto, CreateRoomDTO.class);
            if (!errors.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("status", "VALIDATION_ERROR", "errors", errors));
                return;
            }
            RoomDTO created = roomService.createRoom(dto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            sendJsonResponse(resp, created);

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    //update room
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || "/".equals(pathInfo)) {
                sendErrorResponse(resp, 400, "Room ID is required");
                return;
            }
            int id = Integer.parseInt(pathInfo.substring(1));
            UpdateRoomDTO dto = mapper.readValue(req.getReader(), UpdateRoomDTO.class);

            Map<String, String> errors =
                    validatorContext.validate(dto, UpdateRoomDTO.class);

            if (!errors.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("status", "VALIDATION_ERROR", "errors", errors));
                return;
            }
            RoomDTO updated = roomService.updateRoomDetails(id, dto);
            sendJsonResponse(resp, updated);

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    //delete room
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || "/".equals(pathInfo)) {
                sendErrorResponse(resp, 400, "Room ID is required");
                return;
            }

            int id = Integer.parseInt(pathInfo.substring(1));
            roomService.deleteRoom(id);

            sendJsonResponse(resp, Map.of("status", "DELETED"));

        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    //update room status
    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();

            // {id}/status
            if (pathInfo == null) {
                sendErrorResponse(resp, 404, "Endpoint not found");
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length != 3 || !"status".equals(parts[2])) {
                sendErrorResponse(resp, 404, "Endpoint not found");
                return;
            }
            int id = Integer.parseInt(parts[1]);

            RoomStatusDTO dto = mapper.readValue(req.getReader(), RoomStatusDTO.class);
            roomService.updateRoomStatus(id, dto.isAvailable());
            sendJsonResponse(resp, Map.of("status", "SUCCESS"));

        } catch (Exception e) {
            handleException(resp, e);
        }
    }
}
