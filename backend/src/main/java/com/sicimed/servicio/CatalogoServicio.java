package com.sicimed.servicio;

import com.sicimed.dto.EspecialidadRequest;
import com.sicimed.dto.EspecialidadResponse;
import com.sicimed.dto.MedicamentoRequest;
import com.sicimed.dto.MedicamentoResponse;
import com.sicimed.dto.SedeRequest;
import com.sicimed.dto.SedeResponse;
import com.sicimed.excepcion.ConflictoException;
import com.sicimed.excepcion.RecursoNoEncontradoException;
import com.sicimed.excepcion.ReglaNegocioException;
import com.sicimed.modelo.Especialidad;
import com.sicimed.modelo.Medicamento;
import com.sicimed.modelo.Sede;
import com.sicimed.repositorio.EspecialidadRepositorio;
import com.sicimed.repositorio.MedicamentoRepositorio;
import com.sicimed.repositorio.SedeRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoServicio {

    private final SedeRepositorio sedeRepositorio;
    private final EspecialidadRepositorio especialidadRepositorio;
    private final MedicamentoRepositorio medicamentoRepositorio;

    public CatalogoServicio(SedeRepositorio sedeRepositorio,
                            EspecialidadRepositorio especialidadRepositorio,
                            MedicamentoRepositorio medicamentoRepositorio) {
        this.sedeRepositorio = sedeRepositorio;
        this.especialidadRepositorio = especialidadRepositorio;
        this.medicamentoRepositorio = medicamentoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<SedeResponse> listarSedes(boolean incluirInactivas) {
        List<Sede> sedes = incluirInactivas
                ? sedeRepositorio.findAllByOrderByNombre()
                : sedeRepositorio.findByActivoTrueOrderByNombre();
        return sedes.stream().map(s -> new SedeResponse(
                s.getId(), s.getNombre(), s.getDireccion(), s.getTelefono(), s.isActivo())).toList();
    }

    @Transactional
    public SedeResponse crearSede(SedeRequest request) {
        Sede sede = new Sede();
        sede.setNombre(request.nombre().trim());
        sede.setDireccion(request.direccion());
        sede.setTelefono(request.telefono());
        sede.setActivo(request.activo() == null || request.activo());
        return aSedeResponse(sedeRepositorio.save(sede));
    }

    @Transactional
    public SedeResponse actualizarSede(Long id, SedeRequest request) {
        Sede sede = sedeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada"));
        sede.setNombre(request.nombre().trim());
        sede.setDireccion(request.direccion());
        sede.setTelefono(request.telefono());
        if (request.activo() != null) {
            sede.setActivo(request.activo());
        }
        return aSedeResponse(sedeRepositorio.save(sede));
    }

    @Transactional
    public void desactivarSede(Long id) {
        Sede sede = sedeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada"));
        sede.setActivo(false);
        sedeRepositorio.save(sede);
    }

    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listarEspecialidades() {
        return especialidadRepositorio.findAllByOrderByNombre().stream()
                .map(e -> new EspecialidadResponse(e.getId(), e.getNombre(), e.getDescripcion()))
                .toList();
    }

    @Transactional
    public EspecialidadResponse crearEspecialidad(EspecialidadRequest request) {
        if (especialidadRepositorio.findAll().stream()
                .anyMatch(e -> e.getNombre().equalsIgnoreCase(request.nombre().trim()))) {
            throw new ConflictoException("Ya existe una especialidad con ese nombre");
        }
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(request.nombre().trim());
        especialidad.setDescripcion(request.descripcion());
        return aEspecialidadResponse(especialidadRepositorio.save(especialidad));
    }

    @Transactional
    public EspecialidadResponse actualizarEspecialidad(Long id, EspecialidadRequest request) {
        Especialidad especialidad = especialidadRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada"));
        especialidad.setNombre(request.nombre().trim());
        especialidad.setDescripcion(request.descripcion());
        return aEspecialidadResponse(especialidadRepositorio.save(especialidad));
    }

    @Transactional
    public void eliminarEspecialidad(Long id) {

        especialidadRepositorio.deleteById(especialidadRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad no encontrada")).getId());
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponse> listarMedicamentos(boolean incluirInactivos) {
        List<Medicamento> medicamentos = incluirInactivos
                ? medicamentoRepositorio.findAllByOrderByNombre()
                : medicamentoRepositorio.findByActivoTrueOrderByNombre();
        return medicamentos.stream().map(m -> new MedicamentoResponse(
                m.getId(), m.getNombre(), m.getDescripcion(), m.isActivo())).toList();
    }

    @Transactional
    public MedicamentoResponse crearMedicamento(MedicamentoRequest request) {
        Medicamento medicamento = new Medicamento();
        medicamento.setNombre(request.nombre().trim());
        medicamento.setDescripcion(request.descripcion());
        medicamento.setActivo(request.activo() == null || request.activo());
        return aMedicamentoResponse(medicamentoRepositorio.save(medicamento));
    }

    @Transactional
    public MedicamentoResponse actualizarMedicamento(Long id, MedicamentoRequest request) {
        Medicamento medicamento = medicamentoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado"));
        medicamento.setNombre(request.nombre().trim());
        medicamento.setDescripcion(request.descripcion());
        if (request.activo() != null) {
            medicamento.setActivo(request.activo());
        }
        return aMedicamentoResponse(medicamentoRepositorio.save(medicamento));
    }

    @Transactional
    public void desactivarMedicamento(Long id) {
        Medicamento medicamento = medicamentoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado"));
        medicamento.setActivo(false);
        medicamentoRepositorio.save(medicamento);
    }

    private static SedeResponse aSedeResponse(Sede s) {
        return new SedeResponse(s.getId(), s.getNombre(), s.getDireccion(), s.getTelefono(), s.isActivo());
    }

    private static EspecialidadResponse aEspecialidadResponse(Especialidad e) {
        return new EspecialidadResponse(e.getId(), e.getNombre(), e.getDescripcion());
    }

    private static MedicamentoResponse aMedicamentoResponse(Medicamento m) {
        return new MedicamentoResponse(m.getId(), m.getNombre(), m.getDescripcion(), m.isActivo());
    }
}
