package com.sicimed.servlet;

import com.sicimed.service.AuthService;
import com.sicimed.util.JsonUtil;
import com.sicimed.util.SchemaInitializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/login")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SchemaInitializer.ensureInitialized();
        try {
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            String username = body.get("username") != null ? body.get("username").toString() : "";
            String password = body.get("password") != null ? body.get("password").toString() : "";
            Map<String, Object> result = authService.login(username, password);
            JsonUtil.writeJson(response, HttpServletResponse.SC_OK, result);
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
