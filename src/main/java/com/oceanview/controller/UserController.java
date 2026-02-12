package com.oceanview.controller;
import com.oceanview.dto.LoginRequestDTO;
import com.oceanview.dto.UserDTO;
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("/login".equals(req.getPathInfo())) {
            try {
                LoginRequestDTO loginReq = mapper.readValue(req.getReader(), LoginRequestDTO.class);
                UserDTO user = userService.authenticate(loginReq);
                if (user != null) {
                    sendJsonResponse(resp, user);
                } else {
                    sendErrorResponse(resp, 401, "Invalid Username or Password");
                }
            } catch (Exception e) {
                sendErrorResponse(resp, 400, "Bad Request Format");
            }
        } else {
            sendErrorResponse(resp, 404, "Endpoint not found");
        }
    }

}
