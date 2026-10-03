package com.sicimed.servicio;

import com.sicimed.dto.PacienteResponse;
import com.sicimed.excepcion.RecursoNoEncontradoException;
import com.sicimed.excepcion.ReglaNegocioException;
import com.sicimed.modelo.Paciente;
import com.sicimed.repositorio.PacienteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteServicio {

    private final PacienteRepositorio pacienteRepositorio;

    public PacienteServicio(PacienteRepositorio pacienteRepositorio) {
        this.pacienteRepositorio = pacienteRepositorio;
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscarPorDni(String dni) {
        if (dni == null || dni.isBlank()) {
            throw new ReglaNegocioException("Debe indicar el DNI");
        }
        String dniLimpio = dni.trim();
        Paciente paciente = pacienteRepositorio.findByDni(dniLimpio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado con DNI " + dniLimpio));
        return aResponse(paciente);
    }

    private static PacienteResponse aResponse(Paciente p) {
        return new PacienteResponse(
                p.getId(),
                p.getUsuario().getId(),
                p.getUsuario().getNombreCompleto(),
                p.getDni(),
                p.getFechaNacimiento(),
                p.getTelefono());
    }
}