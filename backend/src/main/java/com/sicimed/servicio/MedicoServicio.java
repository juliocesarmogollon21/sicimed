package com.sicimed.servicio;

import com.sicimed.dto.MedicoRequest;
import com.sicimed.dto.MedicoResponse;
import com.sicimed.excepcion.ConflictoException;
import com.sicimed.excepcion.RecursoNoEncontradoException;
import com.sicimed.excepcion.ReglaNegocioException;
import com.sicimed.modelo.Medico;
import com.sicimed.modelo.Rol;
import com.sicimed.modelo.Usuario;
import com.sicimed.repositorio.MedicoRepositorio;
import com.sicimed.repositorio.EspecialidadRepositorio;
import com.sicimed.repositorio.SedeRepositorio;
import com.sicimed.repositorio.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicoServicio {

    private final MedicoRepositorio medicoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final SedeRepositorio sedeRepositorio;
    private final EspecialidadRepositorio especialidadRepositorio;

    public MedicoServicio(MedicoRepositorio medicoRepositorio,
                          UsuarioRepositorio usuarioRepositorio,
                          SedeRepositorio sedeRepositorio,
                          EspecialidadRepositorio especialidadRepositorio) {
        this.medicoRepositorio = medicoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.sedeRepositorio = sedeRepositorio;
        this.especialidadRepositorio = especialidadRepositorio;
    }

    @Transactional(readOnly = true)
    public List<MedicoResponse> listar(Long sedeId, Long especialidadId, boolean incluirInactivos) {
        return medicoRepositorio.buscarConFiltros(sedeId, especialidadId, incluirInactivos)
                .stream().map(MedicoServicio::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public MedicoResponse obtener(Long id) {
        return aResponse(medicoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medico no encontrado")));
    }

    @Transactional
    public MedicoResponse crear(MedicoRequest request) {
        Usuario usuario = usuarioRepositorio.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (usuario.getRol() != Rol.MEDICO) {
            throw new ReglaNegocioException("El usuario debe tener el rol MEDICO");
        }
        if (medicoRepositorio.existsByUsuarioId(usuario.getId())) {
            throw new ConflictoException("Ese usuario ya esta registrado como medico");
        }

        Medico medico = new Medico();
        medico.setUsuario(usuario);
        medico.setEspecialidad(especialidadRepositorio.findById(request.especialidadId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada")));
        medico.setSede(sedeRepositorio.findById(request.sedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada")));
        medico.setCmp(request.cmp());
        medico.setActivo(request.activo() == null || request.activo());

        return aResponse(medicoRepositorio.save(medico));
    }

    @Transactional
    public MedicoResponse actualizar(Long id, MedicoRequest request) {
        Medico medico = medicoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medico no encontrado"));
        if (request.especialidadId() != null) {
            medico.setEspecialidad(especialidadRepositorio.findById(request.especialidadId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada")));
        }
        if (request.sedeId() != null) {
            medico.setSede(sedeRepositorio.findById(request.sedeId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada")));
        }
        if (request.cmp() != null) {
            medico.setCmp(request.cmp());
        }
        if (request.activo() != null) {
            medico.setActivo(request.activo());
        }
        return aResponse(medicoRepositorio.save(medico));
    }

    @Transactional
    public void desactivar(Long id) {
        Medico medico = medicoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medico no encontrado"));
        medico.setActivo(false);
        medicoRepositorio.save(medico);
    }


    public static MedicoResponse aResponse(Medico m) {
        return new MedicoResponse(
                m.getId(),
                m.getUsuario().getId(),
                m.getUsuario().getNombreCompleto(),
                m.getEspecialidad().getId(),
                m.getEspecialidad().getNombre(),
                m.getSede().getId(),
                m.getSede().getNombre(),
                m.getCmp(),
                m.isActivo());
    }

}
