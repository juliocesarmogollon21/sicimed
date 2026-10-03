package com.sicimed.servicio;

import com.sicimed.dto.CitaRequest;
import com.sicimed.dto.CitaResponse;
import com.sicimed.dto.RecetaRequest;
import com.sicimed.dto.RecetaResponse;
import com.sicimed.excepcion.AccesoDenegadoException;
import com.sicimed.excepcion.ConflictoException;
import com.sicimed.excepcion.RecursoNoEncontradoException;
import com.sicimed.excepcion.ReglaNegocioException;
import com.sicimed.modelo.Cita;
import com.sicimed.modelo.EstadoCita;
import com.sicimed.modelo.Medico;
import com.sicimed.modelo.Paciente;
import com.sicimed.modelo.Receta;
import com.sicimed.modelo.Sede;
import com.sicimed.repositorio.CitaRepositorio;
import com.sicimed.repositorio.MedicoRepositorio;
import com.sicimed.repositorio.PacienteRepositorio;
import com.sicimed.repositorio.RecetaRepositorio;
import com.sicimed.repositorio.SedeRepositorio;
import com.sicimed.seguridad.SicimedPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CitaServicio {

    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);
    private static final int INTERVALO_MINUTOS = 30;

    private final CitaRepositorio citaRepositorio;
    private final MedicoRepositorio medicoRepositorio;
    private final PacienteRepositorio pacienteRepositorio;
    private final SedeRepositorio sedeRepositorio;
    private final RecetaRepositorio recetaRepositorio;

    public CitaServicio(CitaRepositorio citaRepositorio,
                        MedicoRepositorio medicoRepositorio,
                        PacienteRepositorio pacienteRepositorio,
                        SedeRepositorio sedeRepositorio,
                        RecetaRepositorio recetaRepositorio) {
        this.citaRepositorio = citaRepositorio;
        this.medicoRepositorio = medicoRepositorio;
        this.pacienteRepositorio = pacienteRepositorio;
        this.sedeRepositorio = sedeRepositorio;
        this.recetaRepositorio = recetaRepositorio;
    }

    @Transactional(readOnly = true)
    public Page<CitaResponse> listarParaUsuario(SicimedPrincipal principal, String dni,
                                               Long medicoId, LocalDate fecha, Pageable pageable) {

        Specification<Cita> filtro = (raiz, consulta, cb) -> cb.conjunction();

        if (dni != null && !dni.isBlank()) {
            filtro = filtro.and(specPorDni(dni.trim()));
        }
        if (medicoId != null) {
            filtro = filtro.and(specPorMedico(medicoId));
        }
        if (fecha != null) {
            filtro = filtro.and(specPorFecha(fecha));
        }

        if (principal.esGestor()) {

            return citaRepositorio.findAll(filtro, pageable).map(CitaServicio::aResponse);
        }
        if (principal.esMedico()) {

            Specification<Cita> propia = specPorMedico(principal.getMedicoId());
            return citaRepositorio.findAll(propia.and(filtro), pageable).map(CitaServicio::aResponse);
        }

        return citaRepositorio.findAll(specPorPaciente(principal.getPacienteId()).and(filtro), pageable)
                .map(CitaServicio::aResponse);
    }

    @Transactional(readOnly = true)
    public Page<CitaResponse> reporte(Long medicoId, EstadoCita estado, LocalDate desde,
                                      LocalDate hasta, Pageable pageable) {
        return citaRepositorio.findPaginadoConFiltros(medicoId, estado, desde, hasta, pageable)
                .map(CitaServicio::aResponse);
    }

    @Transactional(readOnly = true)
    public CitaResponse obtenerParaUsuario(SicimedPrincipal principal, Long id) {
        return aResponse(assertPuedeGestionarCita(principal, id));
    }

    @Transactional(readOnly = true)
    public List<String> disponibilidad(Long medicoId, LocalDate fecha) {
        if (medicoId == null || fecha == null) {
            throw new ReglaNegocioException("Debe indicar medico y fecha");
        }
        if (medicoRepositorio.findById(medicoId).isEmpty()) {
            throw new RecursoNoEncontradoException("Medico no encontrado");
        }
        List<LocalTime> ocupados = citaRepositorio.findHorasOcupadas(medicoId, fecha, EstadoCita.CANCELADO);
        List<String> libres = new ArrayList<>();
        for (LocalTime hora = HORA_INICIO;
             !hora.isAfter(HORA_FIN);
             hora = hora.plusMinutes(INTERVALO_MINUTOS)) {
            if (!ocupados.contains(hora)) {
                libres.add(hora.toString());
            }
        }
        return libres;
    }

    @Transactional
    public CitaResponse crear(SicimedPrincipal principal, CitaRequest request) {
        Medico medico = medicoRepositorio.findById(request.medicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Medico no encontrado"));
        Sede sede = sedeRepositorio.findById(request.sedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada"));

        Paciente paciente = resolverPaciente(principal, request);

        if (request.fecha().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("No se puede agendar una cita en una fecha pasada");
        }
        validarConflicto(request.medicoId(), request.fecha(), request.hora(), null);

        Cita cita = new Cita();
        cita.setMedico(medico);
        cita.setPaciente(paciente);
        cita.setSede(sede);
        cita.setFecha(request.fecha());
        cita.setHora(request.hora());
        cita.setMotivo(request.motivo().trim());
        cita.setEstado(EstadoCita.PENDIENTE);

        return aResponse(citaRepositorio.save(cita));
    }

    @Transactional
    public CitaResponse reprogramar(SicimedPrincipal principal, Long id, CitaRequest request) {
        Cita cita = assertPuedeGestionarCita(principal, id);

        if (cita.getEstado() == EstadoCita.CANCELADO) {
            throw new ReglaNegocioException("La cita ya esta cancelada");
        }
        if (cita.getEstado() == EstadoCita.ATENDIDO) {
            throw new ReglaNegocioException("No se puede reprogramar una cita atendida");
        }

        Medico medico = medicoRepositorio.findById(request.medicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Medico no encontrado"));
        Sede sede = sedeRepositorio.findById(request.sedeId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede no encontrada"));
        Paciente paciente = request.pacienteId() != null
                ? pacienteRepositorio.findById(request.pacienteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"))
                : cita.getPaciente();

        if (request.fecha().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("No se puede reprogramar a una fecha pasada");
        }

        validarConflicto(request.medicoId(), request.fecha(), request.hora(), id);

        cita.setMedico(medico);
        cita.setSede(sede);
        cita.setPaciente(paciente);
        cita.setFecha(request.fecha());
        cita.setHora(request.hora());
        cita.setMotivo(request.motivo().trim());

        return aResponse(citaRepositorio.save(cita));
    }

    @Transactional
    public CitaResponse cancelar(SicimedPrincipal principal, Long id) {
        Cita cita = assertPuedeGestionarCita(principal, id);
        if (cita.getEstado() == EstadoCita.CANCELADO) {
            throw new ReglaNegocioException("La cita ya esta cancelada");
        }
        if (cita.getEstado() == EstadoCita.ATENDIDO) {
            throw new ReglaNegocioException("No se puede cancelar una cita atendida");
        }
        cita.setEstado(EstadoCita.CANCELADO);
        return aResponse(citaRepositorio.save(cita));
    }

    @Transactional
    public CitaResponse registrarDiagnostico(SicimedPrincipal principal, Long id, String diagnostico) {
        Cita cita = assertPuedeGestionarCita(principal, id);

        if (principal.esPaciente()) {
            throw new AccesoDenegadoException("Acceso denegado para rol PACIENTE");
        }
        if (principal.esMedico() && !cita.getMedico().getId().equals(principal.getMedicoId())) {
            throw new AccesoDenegadoException("No puede diagnosticar citas de otro medico");
        }
        if (cita.getEstado() == EstadoCita.CANCELADO) {
            throw new ReglaNegocioException("No se puede diagnosticar una cita cancelada");
        }
        if (diagnostico == null || diagnostico.isBlank()) {
            throw new ReglaNegocioException("El diagnostico es obligatorio");
        }

        cita.setDiagnostico(diagnostico.trim());
        cita.setEstado(EstadoCita.ATENDIDO);
        return aResponse(citaRepositorio.save(cita));
    }

    @Transactional
    public RecetaResponse guardarReceta(SicimedPrincipal principal, Long id, RecetaRequest request) {
        Cita cita = assertPuedeGestionarCita(principal, id);

        if (principal.esPaciente()) {
            throw new AccesoDenegadoException("Acceso denegado para rol PACIENTE");
        }
        if (principal.esMedico() && !cita.getMedico().getId().equals(principal.getMedicoId())) {
            throw new AccesoDenegadoException("No puede emitir recetas de otro medico");
        }
        if (cita.getEstado() == EstadoCita.CANCELADO) {
            throw new ReglaNegocioException("No se puede emitir receta de una cita cancelada");
        }

        Receta receta = recetaRepositorio.findByCitaId(id).orElseGet(Receta::new);
        receta.setCita(cita);
        receta.setIndicaciones(request.indicaciones().trim());
        receta.setMedicamentos(request.medicamentos());
        receta.setCreadoEn(LocalDateTime.now());
        receta = recetaRepositorio.save(receta);

        return new RecetaResponse(receta.getId(), cita.getId(),
                receta.getIndicaciones(), receta.getMedicamentos(), receta.getCreadoEn());
    }

    @Transactional(readOnly = true)
    public RecetaResponse obtenerRecetaParaUsuario(SicimedPrincipal principal, Long id) {
        Cita cita = assertPuedeGestionarCita(principal, id);

        if (principal.esPaciente() && cita.getEstado() != EstadoCita.ATENDIDO) {
            throw new RecursoNoEncontradoException(
                    "La receta solo esta disponible cuando la cita esta ATENDIDO");
        }
        Receta receta = recetaRepositorio.findByCitaId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La cita no tiene receta registrada"));
        return new RecetaResponse(receta.getId(), cita.getId(),
                receta.getIndicaciones(), receta.getMedicamentos(), receta.getCreadoEn());
    }

    private Paciente resolverPaciente(SicimedPrincipal principal, CitaRequest request) {

        if (principal.esPaciente()) {
            return pacienteRepositorio.findById(principal.getPacienteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));
        }
        if (request.pacienteId() == null) {
            throw new ReglaNegocioException("Debe indicar el paciente");
        }
        return pacienteRepositorio.findById(request.pacienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));
    }

    private void validarConflicto(Long medicoId, LocalDate fecha, LocalTime hora, Long excludeId) {
        long conflictos = citaRepositorio.contarConflictos(
                medicoId, fecha, hora, EstadoCita.CANCELADO, excludeId);
        if (conflictos > 0) {
            throw new ConflictoException(
                    "Ya existe una cita para ese medico en la fecha y hora indicadas");
        }
    }

    @Transactional(readOnly = true)
    public Cita assertPuedeGestionarCita(SicimedPrincipal principal, Long id) {
        Cita cita = citaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        if (principal.esGestor()) {
            return cita;
        }
        if (principal.esMedico() && cita.getMedico().getId().equals(principal.getMedicoId())) {
            return cita;
        }
        if (principal.esPaciente() && cita.getPaciente().getId().equals(principal.getPacienteId())) {
            return cita;
        }
        throw new AccesoDenegadoException("No puede gestionar citas de otro usuario");
    }

    public static CitaResponse aResponse(Cita cita) {
        return new CitaResponse(
                cita.getId(),
                cita.getMedico().getId(),
                cita.getMedico().getUsuario().getNombreCompleto(),
                cita.getPaciente().getId(),
                cita.getPaciente().getUsuario().getNombreCompleto(),
                cita.getSede().getId(),
                cita.getSede().getNombre(),
                cita.getMedico().getEspecialidad().getNombre(),
                cita.getFecha(),
                cita.getHora(),
                cita.getEstado(),
                cita.getMotivo(),
                cita.getDiagnostico());
    }

    private static Specification<Cita> specPorDni(String dni) {
        return (raiz, consulta, cb) -> cb.equal(
                raiz.get("paciente").get("dni"), dni);
    }

    private static Specification<Cita> specPorMedico(Long medicoId) {
        return (raiz, consulta, cb) -> cb.equal(
                raiz.get("medico").get("id"), medicoId);
    }


    private static Specification<Cita> specPorFecha(LocalDate fecha) {
        return (raiz, consulta, cb) -> cb.equal(raiz.get("fecha"), fecha);
    }

    private static Specification<Cita> specPorPaciente(Long pacienteId) {
        return (raiz, consulta, cb) -> cb.equal(
                raiz.get("paciente").get("id"), pacienteId);
    }
}
