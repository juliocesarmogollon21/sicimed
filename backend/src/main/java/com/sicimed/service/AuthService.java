package com.sicimed.service;

import com.sicimed.dao.MedicoDao;
import com.sicimed.dao.PacienteDao;
import com.sicimed.dao.UsuarioDao;
import com.sicimed.model.Paciente;
import com.sicimed.model.Rol;
import com.sicimed.model.Usuario;
import com.sicimed.util.TokenStore;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final MedicoDao medicoDao = new MedicoDao();
    private final PacienteDao pacienteDao = new PacienteDao();

    public Map<String, Object> login(String username, String password) throws SQLException {
        Usuario usuario = usuarioDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        if (!usuario.isActivo() || !BCrypt.checkpw(password, usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        return buildAuthResponse(usuario);
    }

    public Map<String, Object> register(String username, String password, String nombreCompleto,
                                        String email, String dni, String telefono) throws SQLException {
        username = trim(username);
        password = password == null ? "" : password;
        nombreCompleto = trim(nombreCompleto);
        email = trim(email);
        dni = trim(dni);
        telefono = trim(telefono);

        if (username.isEmpty() || password.isEmpty() || nombreCompleto.isEmpty()
                || email.isEmpty() || dni.isEmpty()) {
            throw new IllegalArgumentException("Complete usuario, contraseña, nombre, email y DNI");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (usuarioDao.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }
        if (pacienteDao.findByDni(dni).isPresent()) {
            throw new IllegalArgumentException("El DNI ya está registrado");
        }

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        u.setNombreCompleto(nombreCompleto);
        u.setEmail(email);
        u.setRol(Rol.PACIENTE);
        u.setActivo(true);
        u = usuarioDao.insert(u);

        Paciente p = new Paciente();
        p.setUsuarioId(u.getId());
        p.setDni(dni);
        p.setTelefono(telefono.isEmpty() ? null : telefono);
        pacienteDao.insert(p);

        return buildAuthResponse(u);
    }

    private Map<String, Object> buildAuthResponse(Usuario usuario) throws SQLException {
        String token = TokenStore.issue(usuario);
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("username", usuario.getUsername());
        resp.put("nombreCompleto", usuario.getNombreCompleto());
        resp.put("rol", usuario.getRol().name());
        resp.put("userId", usuario.getId());
        resp.put("medicoId", medicoDao.findByUsuarioId(usuario.getId()).map(m -> m.getId()).orElse(null));
        resp.put("pacienteId", pacienteDao.findByUsuarioId(usuario.getId()).map(p -> p.getId()).orElse(null));
        return resp;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
