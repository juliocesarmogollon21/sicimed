package com.sicimed.servicio;

import com.sicimed.dto.EspecialidadRequest;
import com.sicimed.dto.EspecialidadResponse;
import com.sicimed.excepcion.ConflictoException;
import com.sicimed.excepcion.RecursoNoEncontradoException;
import com.sicimed.modelo.Especialidad;
import com.sicimed.repositorio.EspecialidadRepositorio;
import com.sicimed.repositorio.MedicoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspecialidadServicio {

    private final EspecialidadRepositorio especialidadRepositorio;
    private final MedicoRepositorio medicoRepositorio;

    public EspecialidadServicio(EspecialidadRepositorio especialidadRepositorio,
                                MedicoRepositorio medicoRepositorio) {
        this.especialidadRepositorio = especialidadRepositorio;
        this.medicoRepositorio = medicoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listar() {
        return especialidadRepositorio.findAllByOrderByNombre().stream()
                .map(EspecialidadServicio::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EspecialidadResponse obtener(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional
    public EspecialidadResponse crear(EspecialidadRequest request) {
        String nombre = request.nombre().trim();
        if (especialidadRepositorio.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe una especialidad con ese nombre");
        }
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(nombre);
        especialidad.setDescripcion(request.descripcion());
        return aResponse(especialidadRepositorio.save(especialidad));
    }

    @Transactional
    public EspecialidadResponse actualizar(Long id, EspecialidadRequest request) {
        Especialidad especialidad = buscar(id);
        String nombre = request.nombre().trim();
        if (especialidadRepositorio.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe otra especialidad con ese nombre");
        }
        especialidad.setNombre(nombre);
        especialidad.setDescripcion(request.descripcion());
        return aResponse(especialidadRepositorio.save(especialidad));
    }

    @Transactional
    public void eliminar(Long id) {
        Especialidad especialidad = buscar(id);
        if (medicoRepositorio.existsByEspecialidadId(id)) {
            throw new ConflictoException(
                    "No se puede eliminar: hay medicos registrados con esta especialidad");
        }
        especialidadRepositorio.delete(especialidad);
    }

    private Especialidad buscar(Long id) {
        return especialidadRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));
    }

    private static EspecialidadResponse aResponse(Especialidad e) {
        return new EspecialidadResponse(e.getId(), e.getNombre(), e.getDescripcion());
    }
}