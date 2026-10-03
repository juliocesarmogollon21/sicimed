package com.sicimed.servicio;

import com.sicimed.dto.UsuarioRequest;
import com.sicimed.dto.UsuarioResponse;
import com.sicimed.excepcion.ConflictoException;
import com.sicimed.excepcion.RecursoNoEncontradoException;
import com.sicimed.excepcion.ReglaNegocioException;
import com.sicimed.modelo.Usuario;
import com.sicimed.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Boolean activo) {
        return usuarioRepositorio.findFiltrando(activo).stream()
                .map(UsuarioServicio::aResponse).toList();
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String username = request.username().trim();
        if (usuarioRepositorio.existsByUsernameIgnoreCase(username)) {
            throw new ConflictoException("El nombre de usuario ya existe");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ReglaNegocioException("La contrasena es obligatoria");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setEmail(request.email() == null ? null : request.email().trim());
        usuario.setRol(request.rol());
        usuario.setActivo(request.activo() == null || request.activo());
        return aResponse(usuarioRepositorio.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (request.username() != null && !request.username().isBlank()
                && !request.username().trim().equalsIgnoreCase(usuario.getUsername())) {
            if (usuarioRepositorio.existsByUsernameIgnoreCase(request.username().trim())) {
                throw new ConflictoException("El nombre de usuario ya existe");
            }
            usuario.setUsername(request.username().trim());
        }

        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        if (request.email() != null) {
            usuario.setEmail(request.email().trim());
        }
        if (request.rol() != null) {
            usuario.setRol(request.rol());
        }
        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }
        return aResponse(usuarioRepositorio.save(usuario));
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        usuario.setActivo(false);
        usuarioRepositorio.save(usuario);
    }

    private static UsuarioResponse aResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getNombreCompleto(),
                u.getEmail(), u.getRol(), u.isActivo());
    }
}
