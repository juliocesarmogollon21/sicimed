package com.sicimed.servicio;

import com.sicimed.dto.MedicamentoRequest;
import com.sicimed.dto.MedicamentoResponse;
import com.sicimed.excepcion.RecursoNoEncontradoException;
import com.sicimed.modelo.Medicamento;
import com.sicimed.repositorio.MedicamentoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicamentoServicio {

    private final MedicamentoRepositorio medicamentoRepositorio;

    public MedicamentoServicio(MedicamentoRepositorio medicamentoRepositorio) {
        this.medicamentoRepositorio = medicamentoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponse> listar(boolean incluirInactivos) {
        List<Medicamento> medicamentos = incluirInactivos
                ? medicamentoRepositorio.findAllByOrderByNombre()
                : medicamentoRepositorio.findByActivoTrueOrderByNombre();
        return medicamentos.stream().map(MedicamentoServicio::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public MedicamentoResponse obtener(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional
    public MedicamentoResponse crear(MedicamentoRequest request) {
        Medicamento medicamento = new Medicamento();
        medicamento.setNombre(request.nombre().trim());
        medicamento.setDescripcion(request.descripcion());
        medicamento.setActivo(request.activo() == null || request.activo());
        return aResponse(medicamentoRepositorio.save(medicamento));
    }

    @Transactional
    public MedicamentoResponse actualizar(Long id, MedicamentoRequest request) {
        Medicamento medicamento = buscar(id);
        medicamento.setNombre(request.nombre().trim());
        medicamento.setDescripcion(request.descripcion());
        if (request.activo() != null) {
            medicamento.setActivo(request.activo());
        }
        return aResponse(medicamentoRepositorio.save(medicamento));
    }

    /** Baja logica: las recetas emitidas siguen mostrando el medicamento. */
    @Transactional
    public void desactivar(Long id) {
        Medicamento medicamento = buscar(id);
        medicamento.setActivo(false);
        medicamentoRepositorio.save(medicamento);
    }

    private Medicamento buscar(Long id) {
        return medicamentoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medicamento no encontrado"));
    }

    private static MedicamentoResponse aResponse(Medicamento m) {
        return new MedicamentoResponse(m.getId(), m.getNombre(), m.getDescripcion(), m.isActivo());
    }
}