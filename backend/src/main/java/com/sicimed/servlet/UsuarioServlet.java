package com.sicimed.servlet;

import com.sicimed.dao.UsuarioDao;
import com.sicimed.model.Rol;
import com.sicimed.model.Usuario;
import com.sicimed.util.AuthUtil;
import com.sicimed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/usuarios")
public class UsuarioServlet extends HttpServlet {

    private final UsuarioDao dao = new UsuarioDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            AuthUtil.requireRole(request, Rol.ADMIN);
            List<Map<String, Object>> safe = dao.findAll().stream().map(this::safe).collect(Collectors.toList());
            JsonUtil.writeJson(response, 200, safe);
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
            if (dao.findByUsername(body.get("username").toString()).isPresent()) {
                JsonUtil.writeError(response, 409, "Username ya existe");
                return;
            }
            Usuario u = new Usuario();
            u.setUsername(body.get("username").toString());
            u.setPassword(BCrypt.hashpw(body.get("password").toString(), BCrypt.gensalt()));
            u.setNombreCompleto(body.get("nombreCompleto").toString());
            u.setEmail(body.get("email") != null ? body.get("email").toString() : null);
            u.setRol(Rol.valueOf(body.get("rol").toString()));
            u.setActivo(body.get("activo") == null || Boolean.parseBoolean(body.get("activo").toString()));
            JsonUtil.writeJson(response, 201, safe(dao.insert(u)));
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
            Usuario u = dao.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            if (body.get("username") != null) u.setUsername(body.get("username").toString());
            if (body.get("nombreCompleto") != null) u.setNombreCompleto(body.get("nombreCompleto").toString());
            if (body.get("email") != null) u.setEmail(body.get("email").toString());
            if (body.get("rol") != null) u.setRol(Rol.valueOf(body.get("rol").toString()));
            if (body.get("activo") != null) u.setActivo(Boolean.parseBoolean(body.get("activo").toString()));
            if (body.get("password") != null && !body.get("password").toString().isBlank()) {
                u.setPassword(BCrypt.hashpw(body.get("password").toString(), BCrypt.gensalt()));
            } else {
                u.setPassword(null); // no cambiar
            }
            dao.update(u);
            JsonUtil.writeJson(response, 200, safe(dao.findById(id).orElse(u)));
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
            dao.softDelete(toLong(request.getParameter("id")));
            JsonUtil.writeJson(response, 200, Map.of("ok", true));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 400, e.getMessage());
        }
    }

    private Map<String, Object> safe(Usuario u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("nombreCompleto", u.getNombreCompleto());
        m.put("email", u.getEmail());
        m.put("rol", u.getRol().name());
        m.put("activo", u.isActivo());
        return m;
    }

    private static Long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return Long.parseLong(o.toString());
    }
}
