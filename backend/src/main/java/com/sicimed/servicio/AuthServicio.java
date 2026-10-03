package com.sicimed.servicio;

import com.sicimed.dto.AuthResponse;
import com.sicimed.dto.LoginRequest;
import com.sicimed.dto.RegisterRequest;
import com.sicimed.excepcion.ConflictoException;
import com.sicimed.excepcion.ReglaNegocioException;
import com.sicimed.modelo.Medico;
import com.sicimed.modelo.Paciente;
import com.sicimed.modelo.Rol;
import com.sicimed.modelo.Usuario;
import com.sicimed.repositorio.MedicoRepositorio;
import com.sicimed.repositorio.PacienteRepositorio;
import com.sicimed.repositorio.UsuarioRepositorio;
import com.sicimed.seguridad.JwtServicio;
import com.sicimed.seguridad.SicimedPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final MedicoRepositorio medicoRepositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtServicio jwtServicio;

    public AuthServicio(UsuarioRepositorio usuarioRepositorio,
                        MedicoRepositorio medicoRepositorio,
                        PacienteRepositorio pacienteRepositorio,
                        PasswordEncoder passwordEncoder,
                        JwtServicio jwtServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.medicoRepositorio = medicoRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtServicio = jwtServicio;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepositorio.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ReglaNegocioException("Credenciales invalidas"));

        if (!usuario.isActivo() || !passwordEncoder.matches(request.password(), usuario.getPassword())) {

            throw new ReglaNegocioException("Credenciales invalidas");
        }
        return construirRespuesta(usuario);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String dni = request.dni().trim();

        if (username.length() < 3) {
            throw new ReglaNegocioException("El usuario debe tener al menos 3 caracteres");
        }
        if (usuarioRepositorio.existsByUsernameIgnoreCase(username)) {
            throw new ConflictoException("El nombre de usuario ya esta registrado");
        }
        if (pacienteRepositorio.existsByDni(dni)) {
            throw new ReglaNegocioException("El DNI ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setEmail(request.email().trim());
        usuario.setRol(Rol.PACIENTE);
        usuario.setActivo(true);
        usuario = usuarioRepositorio.save(usuario);

        Paciente paciente = new Paciente();
        paciente.setUsuario(usuario);
        paciente.setDni(dni);
        paciente.setTelefono(request.telefono() == null ? null : request.telefono().trim());
        pacienteRepositorio.save(paciente);

        return construirRespuesta(usuario);
    }

    private AuthResponse construirRespuesta(Usuario usuario) {
        Long medicoId = null;
        Long pacienteId = null;

        if (usuario.getRol() == Rol.MEDICO) {
            medicoId = medicoRepositorio.findByUsuarioId(usuario.getId()).map(Medico::getId).orElse(null);
        } else if (usuario.getRol() == Rol.PACIENTE) {
            pacienteId = pacienteRepositorio.findByUsuarioId(usuario.getId()).map(Paciente::getId).orElse(null);
        }

        SicimedPrincipal principal = new SicimedPrincipal(
                usuario.getId(), usuario.getUsername(), usuario.getNombreCompleto(),
                usuario.getRol(), medicoId, pacienteId);

        return new AuthResponse(
                jwtServicio.generar(principal),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                usuario.getId(),
                medicoId,
                pacienteId);
    }
}
