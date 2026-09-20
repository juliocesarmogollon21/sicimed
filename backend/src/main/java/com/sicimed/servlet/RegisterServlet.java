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

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SchemaInitializer.ensureInitialized();
        try {
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            String username = str(body, "username");
            String password = str(body, "password");
            String nombreCompleto = str(body, "nombreCompleto");
            String email = str(body, "email");
            String dni = str(body, "dni");
            String telefono = str(body, "telefono");
            Map<String, Object> result = authService.register(
                    username, password, nombreCompleto, email, dni, telefono);
            JsonUtil.writeJson(response, HttpServletResponse.SC_CREATED, result);
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static String str(Map<?, ?> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : "";
    }
}
