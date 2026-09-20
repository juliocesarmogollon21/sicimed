package com.sicimed.servlet;

import com.sicimed.dao.MedicoDao;
import com.sicimed.model.Medico;
import com.sicimed.model.Rol;
import com.sicimed.util.AuthUtil;
import com.sicimed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/medicos")
public class MedicoRootServlet extends HttpServlet {

    private final MedicoDao medicoDao = new MedicoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            boolean all = "1".equals(request.getParameter("all")) || "true".equalsIgnoreCase(request.getParameter("all"));
            if (all) {
                AuthUtil.requireRole(request, Rol.ADMIN);
                JsonUtil.writeJson(response, 200, medicoDao.findAll());
                return;
            }
            Long sedeId = parseLong(request.getParameter("sedeId"));
            Long espId = parseLong(request.getParameter("especialidadId"));
            JsonUtil.writeJson(response, 200, medicoDao.findFiltrado(sedeId, espId));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            AuthUtil.requireRole(request, Rol.ADMIN);
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            Medico m = new Medico();
            m.setUsuarioId(toLong(body.get("usuarioId")));
            m.setEspecialidadId(toLong(body.get("especialidadId")));
            m.setSedeId(toLong(body.get("sedeId")));
            m.setCmp(body.get("cmp") != null ? body.get("cmp").toString() : null);
            m.setActivo(body.get("activo") == null || Boolean.parseBoolean(body.get("activo").toString()));
            JsonUtil.writeJson(response, 201, medicoDao.insert(m));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, e.getMessage());
        }
    }

    private static Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        return Long.parseLong(s);
    }

    private static Long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
