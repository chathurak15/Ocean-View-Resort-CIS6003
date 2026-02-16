package com.oceanview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceanview.exception.DataAccessException;
import com.oceanview.exception.DuplicateResourceException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class BaseServlet extends HttpServlet {
    protected final ObjectMapper mapper = new ObjectMapper();

    protected void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        mapper.writeValue(out, data);
        out.flush();
    }

    protected void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), Map.of("error", message));
    }

    protected void handleException(HttpServletResponse resp,Exception e) throws IOException {
        if (e instanceof DuplicateResourceException) {
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
            return;
        }

        if (e instanceof DataAccessException) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
            return;
        }
        // default
        sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
    }
}
