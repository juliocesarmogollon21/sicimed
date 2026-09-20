package com.sicimed.servlet;

import com.sicimed.dao.EspecialidadDao;
import com.sicimed.model.Especialidad;
import com.sicimed.model.Rol;
import com.sicimed.util.AuthUtil;
import com.sicimed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/especialidades")
public class EspecialidadServlet extends HttpServlet {

    private final EspecialidadDao dao = new EspecialidadDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            JsonUtil.writeJson(response, 200, dao.findAll());
        } catch (Exception e) {
            JsonUtil.writeError(response, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            AuthUtil.requireRole(request, Rol.ADMIN);
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            Especialidad e = new Especialidad();
            e.setNombre(body.get("nombre").toString());
            e.setDescripcion(body.get("descripcion") != null ? body.get("descripcion").toString() : null);
            JsonUtil.writeJson(response, 201, dao.insert(e));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            AuthUtil.requireRole(request, Rol.ADMIN);
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            Long id = toLong(body.get("id"));
            Especialidad e = dao.findById(id).orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
            if (body.get("nombre") != null) e.setNombre(body.get("nombre").toString());
            if (body.get("descripcion") != null) e.setDescripcion(body.get("descripcion").toString());
            dao.update(e);
            JsonUtil.writeJson(response, 200, dao.findById(id).orElse(e));
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
            Long id = toLong(request.getParameter("id"));
            dao.delete(id);
            JsonUtil.writeJson(response, 200, Map.of("ok", true));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, e.getMessage());
        }
    }

    private static Long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
