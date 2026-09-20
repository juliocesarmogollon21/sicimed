package com.sicimed.servlet;

import com.sicimed.dao.PacienteDao;
import com.sicimed.model.Rol;
import com.sicimed.util.AuthUtil;
import com.sicimed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/pacientes")
public class PacienteServlet extends HttpServlet {

    private final PacienteDao dao = new PacienteDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            AuthUtil.requireRole(request, Rol.ADMIN, Rol.RECEPCIONISTA, Rol.MEDICO);
            String dni = request.getParameter("dni");
            if (dni != null && !dni.isBlank()) {
                var p = dao.findByDni(dni.trim());
                if (p.isEmpty()) {
                    JsonUtil.writeError(response, 404, "Paciente no encontrado con DNI " + dni);
                    return;
                }
                JsonUtil.writeJson(response, 200, p.get());
                return;
            }
            JsonUtil.writeError(response, 400, "Parámetro dni requerido");
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 500, e.getMessage());
        }
    }
}
