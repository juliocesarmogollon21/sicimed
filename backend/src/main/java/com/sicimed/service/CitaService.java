package com.sicimed.service;

import com.sicimed.dao.*;
import com.sicimed.model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CitaService {

    private final CitaDao citaDao = new CitaDao();
    private final MedicoDao medicoDao = new MedicoDao();
    private final PacienteDao pacienteDao = new PacienteDao();
    private final SedeDao sedeDao = new SedeDao();
    private final RecetaDao recetaDao = new RecetaDao();

    public List<Cita> listar() throws SQLException {
        return citaDao.findAll();
    }

    public List<Cita> listarParaUsuario(Usuario user) throws SQLException {
        if (user.getRol() == Rol.PACIENTE) {
            Paciente p = pacienteDao.findByUsuarioId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Paciente no vinculado al usuario"));
            return citaDao.findByPacienteId(p.getId());
        }
        if (user.getRol() == Rol.MEDICO) {
            Medico m = medicoDao.findByUsuarioId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Médico no vinculado al usuario"));
            return citaDao.findByMedico(m.getId(), null);
        }
        return citaDao.findAll();
    }

    public Cita obtener(Long id) throws SQLException {
        return citaDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
    }

    /**
     * ADMIN / RECEPCIONISTA: siempre.
     * MEDICO: solo si la cita es de su agenda.
     * PACIENTE: solo si la cita le pertenece (lectura de receta / cancelación propia).
     */
    public Cita assertPuedeGestionarCita(Usuario user, Long citaId) throws SQLException {
        Cita cita = obtener(citaId);
        if (user.getRol() == Rol.ADMIN || user.getRol() == Rol.RECEPCIONISTA) {
            return cita;
        }
        if (user.getRol() == Rol.MEDICO) {
            Medico m = medicoDao.findByUsuarioId(user.getId())
                    .orElseThrow(() -> new SecurityException("Médico no vinculado"));
            if (!m.getId().equals(cita.getMedicoId())) {
                throw new SecurityException("No puede gestionar citas de otro médico");
            }
            return cita;
        }
        if (user.getRol() == Rol.PACIENTE) {
            Paciente p = pacienteDao.findByUsuarioId(user.getId())
                    .orElseThrow(() -> new SecurityException("Paciente no vinculado"));
            if (!p.getId().equals(cita.getPacienteId())) {
                throw new SecurityException("No puede acceder a citas de otro paciente");
            }
            return cita;
        }
        throw new SecurityException("Rol no autorizado");
    }

    /** Lectura de receta: mismos criterios de pertenencia (PACIENTE de su cita ATENDIDO). */
    public Receta obtenerRecetaParaUsuario(Usuario user, Long citaId) throws SQLException {
        Cita cita = assertPuedeGestionarCita(user, citaId);
        if (user.getRol() == Rol.PACIENTE && cita.getEstado() != EstadoCita.ATENDIDO) {
            throw new IllegalArgumentException("La receta solo está disponible cuando la cita está ATENDIDO");
        }
        return recetaDao.findByCitaId(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));
    }

    public Cita crear(Long medicoId, Long pacienteId, Long sedeId, LocalDate fecha, LocalTime hora, String motivo)
            throws SQLException {
        medicoDao.findById(medicoId).orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        pacienteDao.findById(pacienteId).orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        sedeDao.findById(sedeId).orElseThrow(() -> new IllegalArgumentException("Sede no encontrada"));

        if (citaDao.existsConflict(medicoId, fecha, hora)) {
            throw new ConflictException("Ya existe una cita para ese médico en la fecha y hora indicadas");
        }

        Cita cita = new Cita();
        cita.setMedicoId(medicoId);
        cita.setPacienteId(pacienteId);
        cita.setSedeId(sedeId);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(motivo);
        cita.setEstado(EstadoCita.PENDIENTE);
        return citaDao.insert(cita);
    }

    public Cita actualizar(Long id, Long medicoId, Long pacienteId, Long sedeId,
                           LocalDate fecha, LocalTime hora, String motivo) throws SQLException {
        Cita cita = obtener(id);
        if (cita.getEstado() == EstadoCita.CANCELADO) {
            throw new IllegalArgumentException("No se puede modificar una cita cancelada");
        }
        if (cita.getEstado() == EstadoCita.ATENDIDO) {
            throw new IllegalArgumentException("No se puede reprogramar una cita atendida");
        }
        boolean cambio = !cita.getMedicoId().equals(medicoId)
                || !cita.getFecha().equals(fecha)
                || !cita.getHora().equals(hora);
        if (cambio && citaDao.existsConflictExcluding(medicoId, fecha, hora, id)) {
            throw new ConflictException("Ya existe una cita para ese médico en la fecha y hora indicadas");
        }
        cita.setMedicoId(medicoId);
        cita.setPacienteId(pacienteId);
        cita.setSedeId(sedeId);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(motivo);
        citaDao.update(cita);
        return obtener(id);
    }

    public Cita cancelar(Long id) throws SQLException {
        Cita cita = obtener(id);
        if (cita.getEstado() == EstadoCita.CANCELADO) {
            throw new IllegalArgumentException("La cita ya está cancelada");
        }
        if (cita.getEstado() == EstadoCita.ATENDIDO) {
            throw new IllegalArgumentException("No se puede cancelar una cita atendida");
        }
        cita.setEstado(EstadoCita.CANCELADO);
        citaDao.update(cita);
        return obtener(id);
    }

    public void eliminar(Long id) throws SQLException {
        obtener(id);
        citaDao.delete(id);
    }

    public List<Cita> agendaMedico(Long medicoId, LocalDate fecha) throws SQLException {
        medicoDao.findById(medicoId).orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        return citaDao.findByMedico(medicoId, fecha);
    }

    public List<String> disponibilidad(Long medicoId, LocalDate fecha) throws SQLException {
        medicoDao.findById(medicoId).orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        List<LocalTime> slots = new ArrayList<>();
        for (int h = 8; h <= 17; h++) {
            slots.add(LocalTime.of(h, 0));
            if (h < 17) slots.add(LocalTime.of(h, 30));
        }
        Set<LocalTime> ocupadas = citaDao.findByMedico(medicoId, fecha).stream()
                .filter(c -> c.getEstado() != EstadoCita.CANCELADO)
                .map(Cita::getHora)
                .collect(Collectors.toSet());
        return slots.stream().filter(t -> !ocupadas.contains(t)).map(LocalTime::toString).toList();
    }

    public Cita registrarDiagnostico(Long id, String diagnostico) throws SQLException {
        Cita cita = obtener(id);
        if (cita.getEstado() == EstadoCita.CANCELADO) {
            throw new IllegalArgumentException("Cita cancelada");
        }
        cita.setDiagnostico(diagnostico);
        cita.setEstado(EstadoCita.ATENDIDO);
        citaDao.update(cita);
        return obtener(id);
    }

    public Receta registrarReceta(Long citaId, String indicaciones, String medicamentos) throws SQLException {
        Cita cita = obtener(citaId);
        if (cita.getEstado() == EstadoCita.CANCELADO) {
            throw new IllegalArgumentException("Cita cancelada");
        }
        Receta r = new Receta();
        r.setCitaId(citaId);
        r.setIndicaciones(indicaciones == null ? "" : indicaciones);
        r.setMedicamentos(medicamentos);
        return recetaDao.save(r);
    }

    public Receta obtenerReceta(Long citaId) throws SQLException {
        return recetaDao.findByCitaId(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));
    }

    /** Excepción de negocio para HTTP 409 */
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }
}
