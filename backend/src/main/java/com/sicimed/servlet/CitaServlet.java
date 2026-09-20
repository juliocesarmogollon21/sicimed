package com.sicimed.servlet;

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

@WebServlet("/api/citas/*")
public class CitaServlet extends HttpServlet {

    private final CitaService citaService = new CitaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String path = request.getPathInfo();
            if (path == null || path.equals("/")) {
                if ("disponibilidad".equals(request.getParameter("action"))) {
                    Long medicoId = Long.parseLong(request.getParameter("medicoId"));
                    LocalDate fecha = LocalDate.parse(request.getParameter("fecha"));
                    JsonUtil.writeJson(response, 200, citaService.disponibilidad(medicoId, fecha));
                    return;
                }
                Usuario user = AuthUtil.requireUser(request);
                JsonUtil.writeJson(response, 200, citaService.listarParaUsuario(user));
                return;
            }
            String[] parts = path.substring(1).split("/");
            if (parts.length == 1 && "disponibilidad".equals(parts[0])) {
                Long medicoId = Long.parseLong(request.getParameter("medicoId"));
                LocalDate fecha = LocalDate.parse(request.getParameter("fecha"));
                JsonUtil.writeJson(response, 200, citaService.disponibilidad(medicoId, fecha));
                return;
            }
            Long id = Long.parseLong(parts[0]);
            Usuario user = AuthUtil.requireUser(request);
            if (parts.length == 1) {
                JsonUtil.writeJson(response, 200, citaService.assertPuedeGestionarCita(user, id));
                return;
            }
            if (parts.length == 2 && "receta".equals(parts[1])) {
                // PACIENTE puede leer la receta de SU cita (assertPuedeGestionarCita + ATENDIDO)
                JsonUtil.writeJson(response, 200, citaService.obtenerRecetaParaUsuario(user, id));
                return;
            }
            JsonUtil.writeError(response, 404, "Recurso no encontrado");
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
        } catch (CitaService.ConflictException e) {
            JsonUtil.writeError(response, 409, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.writeError(response, 404, e.getMessage());
        } catch (Exception e) {
            JsonUtil.writeError(response, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String path = request.getPathInfo();
            if (path == null || path.equals("/")) {
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
                return;
            }
            String[] parts = path.substring(1).split("/");
            Long id = Long.parseLong(parts[0]);
            Usuario user = AuthUtil.requireUser(request);
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            if (parts.length == 2 && "diagnostico".equals(parts[1])) {
                AuthUtil.requireRole(request, Rol.MEDICO, Rol.ADMIN);
                citaService.assertPuedeGestionarCita(user, id);
                JsonUtil.writeJson(response, 200,
                        citaService.registrarDiagnostico(id, body.get("diagnostico").toString()));
                return;
            }
            if (parts.length == 2 && "receta".equals(parts[1])) {
                AuthUtil.requireRole(request, Rol.MEDICO, Rol.ADMIN);
                citaService.assertPuedeGestionarCita(user, id);
                String ind = body.get("indicaciones") != null ? body.get("indicaciones").toString() : "";
                String med = body.get("medicamentos") != null ? body.get("medicamentos").toString() : null;
                JsonUtil.writeJson(response, 200, citaService.registrarReceta(id, ind, med));
                return;
            }
            if (parts.length == 2 && "cancel".equals(parts[1])) {
                citaService.assertPuedeGestionarCita(user, id);
                JsonUtil.writeJson(response, 200, citaService.cancelar(id));
                return;
            }
            JsonUtil.writeError(response, 404, "Recurso no encontrado");
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String path = request.getPathInfo();
            if (path == null || path.equals("/")) {
                JsonUtil.writeError(response, 400, "Id requerido");
                return;
            }
            String[] parts = path.substring(1).split("/");
            Long id = Long.parseLong(parts[0]);
            Usuario user = AuthUtil.requireUser(request);
            if (parts.length == 2 && "cancel".equals(parts[1])) {
                citaService.assertPuedeGestionarCita(user, id);
                JsonUtil.writeJson(response, 200, citaService.cancelar(id));
                return;
            }
            // Reprogramar: ADMIN/RECEPCIONISTA/PACIENTE (propia) / MEDICO (propia agenda)
            AuthUtil.requireRole(request, Rol.ADMIN, Rol.RECEPCIONISTA, Rol.PACIENTE, Rol.MEDICO);
            citaService.assertPuedeGestionarCita(user, id);
            Map<?, ?> body = JsonUtil.fromJson(request, Map.class);
            var cita = citaService.actualizar(
                    id,
                    toLong(body.get("medicoId")),
                    toLong(body.get("pacienteId")),
                    toLong(body.get("sedeId")),
                    LocalDate.parse(body.get("fecha").toString()),
                    LocalTime.parse(body.get("hora").toString()),
                    body.get("motivo") != null ? body.get("motivo").toString() : null
            );
            JsonUtil.writeJson(response, 200, cita);
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

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String path = request.getPathInfo();
            Long id = Long.parseLong(path.substring(1).split("/")[0]);
            Usuario user = AuthUtil.requireUser(request);
            citaService.assertPuedeGestionarCita(user, id);
            JsonUtil.writeJson(response, 200, citaService.cancelar(id));
        } catch (SecurityException e) {
            JsonUtil.writeError(response, 403, e.getMessage());
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
