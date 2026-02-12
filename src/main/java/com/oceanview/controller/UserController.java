package com.oceanview.controller;
import com.oceanview.dto.user.LoginRequestDTO;
import com.oceanview.dto.user.RegisterDTO;
import com.oceanview.dto.user.UpdateStatusDTO;
import com.oceanview.dto.user.UserDTO;
import com.oceanview.exception.ForbiddenOperationException;
import com.oceanview.service.UserService;
import com.oceanview.service.impl.UserServiceImpl;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "UserServlet", urlPatterns = {"/api/users/*"})
public class UserController extends BaseServlet {

    private final UserService userService = new UserServiceImpl();

    //get all users
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            List<UserDTO> allUsers = userService.getAllUsers();
            sendJsonResponse(resp, allUsers);
        } else {
            sendErrorResponse(resp, 404, "Endpoint not found");
        }
    }

    //create user and login user
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            //login user
            if ("/login".equals(pathInfo)) {
                LoginRequestDTO loginReq = mapper.readValue(req.getReader(), LoginRequestDTO.class);
                UserDTO userDTO = userService.authenticate(loginReq);

                if (userDTO != null) {
                    sendJsonResponse(resp, userDTO);
                } else {
                    sendErrorResponse(resp, 401, "Invalid Username or Password");
                }
                return;
            }

            //create user
            if ("/register".equals(pathInfo)) {
                RegisterDTO registerDTO = mapper.readValue(req.getReader(), RegisterDTO.class);
                UserDTO created = userService.createUser(registerDTO);

                if (created != null) {
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    sendJsonResponse(resp, created);
                } else {
                    sendErrorResponse(resp, 409, "Username already exists or invalid data");
                }
                return;
            }

            sendErrorResponse(resp, 404, "Endpoint not found");
        } catch (Exception e) {
            sendErrorResponse(resp, 400, "Bad Request Format");
        }
    }

    //delete user
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || "/".equals(pathInfo)) {
            sendErrorResponse(resp, 400, "User ID is required");
            return;
        }
        try {
            int userId = Integer.parseInt(pathInfo.substring(1));

            try {
                boolean deleted = userService.deleteUser(userId);

                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    sendErrorResponse(resp, 404, "User not found");
                }

            } catch (ForbiddenOperationException ex) {
                sendErrorResponse(resp, 403, ex.getMessage());
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, 400, "Invalid User ID");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();

        if (path == null) {
            sendErrorResponse(resp, 404, "Endpoint not found");
            return;
        }

        String[] parts = path.split("/");
        if (parts.length == 3 && "status".equals(parts[2])) {
            try {
                int userId = Integer.parseInt(parts[1]);
                UpdateStatusDTO body = mapper.readValue(req.getReader(), UpdateStatusDTO.class);

                boolean updated = userService.updateUserStatus(userId, body.isActive());
                if (updated) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    sendErrorResponse(resp, 404, "User not found");
                }
                return;

            } catch (NumberFormatException e) {
                sendErrorResponse(resp, 400, "Invalid User ID");
                return;
            } catch (Exception e) {
                sendErrorResponse(resp, 400, "Bad Request Format");
                return;
            }
        }
        sendErrorResponse(resp, 404, "Endpoint not found");
    }

}
