package com.sicimed.servlet;

import com.sicimed.model.Rol;
import com.sicimed.service.MedicamentoService;
import com.sicimed.util.AuthUtil;
import com.sicimed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/medicamentos")
public class MedicamentoServlet extends HttpServlet {

    private final MedicamentoService service = new MedicamentoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            boolean all = "1".equals(request.getParameter("all"))
                    || "true".equalsIgnoreCase(request.getParameter("all"));
            if (all) {
                AuthUtil.requireRole(request, Rol.ADMIN);
            }
            JsonUtil.writeJson(response, 200, service.listar(all));
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
            boolean activo = body.get("activo") == null || Boolean.parseBoolean(body.get("activo").toString());
            JsonUtil.writeJson(response, 201, service.crear(
                    str(body.get("nombre")),
                    str(body.get("descripcion")),
                    activo
            ));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, 400, e.getMessage());
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
            Boolean activo = body.get("activo") == null ? null : Boolean.parseBoolean(body.get("activo").toString());
            JsonUtil.writeJson(response, 200, service.actualizar(
                    id,
                    str(body.get("nombre")),
                    str(body.get("descripcion")),
                    activo
            ));
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
            service.desactivar(id);
            JsonUtil.writeJson(response, 200, Map.of("ok", true));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, 404, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, e.getMessage());
        }
    }

    private static String str(Object o) { return o == null ? null : o.toString(); }
    private static Long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
