package com.sicimed.servlet;

import com.sicimed.dao.MedicoDao;
import com.sicimed.model.Medico;
import com.sicimed.model.Rol;
import com.sicimed.service.CitaService;
import com.sicimed.util.AuthUtil;
import com.sicimed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@WebServlet("/api/medicos/*")
public class MedicoServlet extends HttpServlet {

    private final MedicoDao medicoDao = new MedicoDao();
    private final CitaService citaService = new CitaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String path = request.getPathInfo();
            if (path == null || path.equals("/")) {
                Long sedeId = parseLong(request.getParameter("sedeId"));
                Long especialidadId = parseLong(request.getParameter("especialidadId"));
                JsonUtil.writeJson(response, 200, medicoDao.findFiltrado(sedeId, especialidadId));
                return;
            }
            String[] parts = path.substring(1).split("/");
            Long id = Long.parseLong(parts[0]);
            if (parts.length == 1) {
                var m = medicoDao.findById(id);
                if (m.isEmpty()) {
                    JsonUtil.writeError(response, 404, "Médico no encontrado");
                } else {
                    JsonUtil.writeJson(response, 200, m.get());
                }
                return;
            }
            if (parts.length == 2 && "agenda".equals(parts[1])) {
                LocalDate fecha = request.getParameter("fecha") != null
                        ? LocalDate.parse(request.getParameter("fecha")) : null;
                JsonUtil.writeJson(response, 200, citaService.agendaMedico(id, fecha));
                return;
            }
            JsonUtil.writeError(response, 404, "Recurso no encontrado");
        } catch (NumberFormatException e) {
            JsonUtil.writeError(response, 400, "Id inválido");
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, 404, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            AuthUtil.requireRole(request, Rol.ADMIN);
            String path = request.getPathInfo();
            Long id = Long.parseLong(path.substring(1).split("/")[0]);
            Medico m = medicoDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            if (body.get("usuarioId") != null) m.setUsuarioId(toLong(body.get("usuarioId")));
            if (body.get("especialidadId") != null) m.setEspecialidadId(toLong(body.get("especialidadId")));
            if (body.get("sedeId") != null) m.setSedeId(toLong(body.get("sedeId")));
            if (body.get("cmp") != null) m.setCmp(body.get("cmp").toString());
            if (body.get("activo") != null) m.setActivo(Boolean.parseBoolean(body.get("activo").toString()));
            medicoDao.update(m);
            JsonUtil.writeJson(response, 200, medicoDao.findById(id).orElse(m));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, 404, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            AuthUtil.requireRole(request, Rol.ADMIN);
            Long id = Long.parseLong(request.getPathInfo().substring(1).split("/")[0]);
            medicoDao.softDelete(id);
            JsonUtil.writeJson(response, 200, Map.of("ok", true));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, e.getMessage());
        }
    }

    private Long parseLong(String v) {
        if (v == null || v.isBlank()) return null;
        return Long.parseLong(v);
    }

    private Long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
