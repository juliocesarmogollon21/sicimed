package com.sicimed.servicio;

import com.sicimed.dto.SedeRequest;
import com.sicimed.dto.SedeResponse;
import com.sicimed.excepcion.RecursoNoEncontradoException;
import com.sicimed.modelo.Sede;
import com.sicimed.repositorio.SedeRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SedeServicio {

    private final SedeRepositorio sedeRepositorio;

    public SedeServicio(SedeRepositorio sedeRepositorio) {
        this.sedeRepositorio = sedeRepositorio;
    }

    @Transactional(readOnly = true)
    public List<SedeResponse> listar(boolean incluirInactivas) {
        List<Sede> sedes = incluirInactivas
                ? sedeRepositorio.findAllByOrderByNombre()
                : sedeRepositorio.findByActivoTrueOrderByNombre();
        return sedes.stream().map(SedeServicio::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public SedeResponse obtener(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional
    public SedeResponse crear(SedeRequest request) {
        Sede sede = new Sede();
        sede.setNombre(request.nombre().trim());
        sede.setDireccion(request.direccion());
        sede.setTelefono(request.telefono());
        sede.setActivo(request.activo() == null || request.activo());
        return aResponse(sedeRepositorio.save(sede));
    }

    @Transactional
    public SedeResponse actualizar(Long id, SedeRequest request) {
        Sede sede = buscar(id);
        sede.setNombre(request.nombre().trim());
        sede.setDireccion(request.direccion());
        sede.setTelefono(request.telefono());
        if (request.activo() != null) {
            sede.setActivo(request.activo());
        }
        return aResponse(sedeRepositorio.save(sede));
    }

    /** Baja logica: la sede queda inactiva y conserva su historial de citas. */
    @Transactional
    public void desactivar(Long id) {
        Sede sede = buscar(id);
        sede.setActivo(false);
        sedeRepositorio.save(sede);
    }

    private Sede buscar(Long id) {
        return sedeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada"));
    }

    private static SedeResponse aResponse(Sede s) {
        return new SedeResponse(s.getId(), s.getNombre(), s.getDireccion(), s.getTelefono(), s.isActivo());
    }
}