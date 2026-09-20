package com.sicimed.servlet;

import com.sicimed.dao.CitaDao;
import com.sicimed.model.Rol;
import com.sicimed.model.Usuario;
import com.sicimed.service.CitaService;
import com.sicimed.util.AuthUtil;
import com.sicimed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/** Maneja /api/citas exacto (sin path extra). */
@WebServlet("/api/citas")
public class CitaRootServlet extends HttpServlet {

    private final CitaService citaService = new CitaService();
    private final CitaDao citaDao = new CitaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Usuario user = AuthUtil.requireUser(request);
            String dni = request.getParameter("dni");
            if (dni != null && !dni.isBlank()) {
                AuthUtil.requireRole(request, Rol.ADMIN, Rol.RECEPCIONISTA);
                JsonUtil.writeJson(response, 200, citaDao.findByPacienteDni(dni.trim()));
                return;
            }
            JsonUtil.writeJson(response, 200, citaService.listarParaUsuario(user));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            AuthUtil.requireRole(request, Rol.ADMIN, Rol.RECEPCIONISTA, Rol.PACIENTE);
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            var cita = citaService.crear(
                    toLong(body.get("medicoId")),
                    toLong(body.get("pacienteId")),
                    toLong(body.get("sedeId")),
                    LocalDate.parse(body.get("fecha").toString()),
                    LocalTime.parse(body.get("hora").toString()),
                    body.get("motivo") != null ? body.get("motivo").toString() : null
            );
            JsonUtil.writeJson(response, 201, cita);
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (CitaService.ConflictException e) {
            JsonUtil.writeError(response, 409, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, 400, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 500, e.getMessage());
        }
    }

    private Long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
