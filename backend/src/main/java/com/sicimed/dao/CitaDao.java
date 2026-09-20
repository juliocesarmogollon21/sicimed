package com.sicimed.dao;

import com.sicimed.model.Cita;
import com.sicimed.model.EstadoCita;
import com.sicimed.util.DbConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CitaDao {

    private static final String BASE = """
        SELECT c.id, c.medico_id, um.nombre_completo AS medico_nombre,
               c.paciente_id, up.nombre_completo AS paciente_nombre,
               c.sede_id, s.nombre AS sede_nombre, e.nombre AS especialidad,
               c.fecha, c.hora, c.estado, c.motivo, c.diagnostico
        FROM citas c
        JOIN medicos m ON m.id = c.medico_id
        JOIN usuarios um ON um.id = m.usuario_id
        JOIN pacientes p ON p.id = c.paciente_id
        JOIN usuarios up ON up.id = p.usuario_id
        JOIN sedes s ON s.id = c.sede_id
        JOIN especialidades e ON e.id = m.especialidad_id
        """;

    public List<Cita> findAll() throws SQLException {
        List<Cita> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + " ORDER BY c.fecha DESC, c.hora DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Cita> findById(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + " WHERE c.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public List<Cita> findByMedico(Long medicoId, LocalDate fecha) throws SQLException {
        String sql = BASE + " WHERE c.medico_id = ?";
        if (fecha != null) sql += " AND c.fecha = ?";
        sql += " ORDER BY c.fecha ASC, c.hora ASC";
        List<Cita> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, medicoId);
            if (fecha != null) ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean existsConflict(Long medicoId, LocalDate fecha, LocalTime hora) throws SQLException {
        return existsConflictExcluding(medicoId, fecha, hora, null);
    }

    public boolean existsConflictExcluding(Long medicoId, LocalDate fecha, LocalTime hora, Long excludeId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM citas
            WHERE medico_id = ? AND fecha = ? AND hora = ? AND estado <> 'CANCELADO'
            """ + (excludeId != null ? " AND id <> ?" : "");
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, medicoId);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(hora));
            if (excludeId != null) ps.setLong(4, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Cita insert(Cita cita) throws SQLException {
        String sql = """
            INSERT INTO citas(medico_id,paciente_id,sede_id,fecha,hora,estado,motivo,diagnostico)
            VALUES (?,?,?,?,?,?,?,?)
            """;
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, cita.getMedicoId());
            ps.setLong(2, cita.getPacienteId());
            ps.setLong(3, cita.getSedeId());
            ps.setDate(4, Date.valueOf(cita.getFecha()));
            ps.setTime(5, Time.valueOf(cita.getHora()));
            ps.setString(6, cita.getEstado().name());
            ps.setString(7, cita.getMotivo());
            ps.setString(8, cita.getDiagnostico());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                cita.setId(rs.getLong(1));
            }
        }
        return findById(cita.getId()).orElse(cita);
    }

    public void update(Cita cita) throws SQLException {
        String sql = """
            UPDATE citas SET medico_id=?, paciente_id=?, sede_id=?, fecha=?, hora=?,
            estado=?, motivo=?, diagnostico=? WHERE id=?
            """;
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, cita.getMedicoId());
            ps.setLong(2, cita.getPacienteId());
            ps.setLong(3, cita.getSedeId());
            ps.setDate(4, Date.valueOf(cita.getFecha()));
            ps.setTime(5, Time.valueOf(cita.getHora()));
            ps.setString(6, cita.getEstado().name());
            ps.setString(7, cita.getMotivo());
            ps.setString(8, cita.getDiagnostico());
            ps.setLong(9, cita.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM citas WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }


    public List<Cita> findByPacienteDni(String dni) throws SQLException {
        String sql = BASE + " WHERE p.dni = ? ORDER BY c.fecha DESC, c.hora DESC";
        List<Cita> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Cita> findByPacienteId(Long pacienteId) throws SQLException {
        String sql = BASE + " WHERE c.paciente_id = ? ORDER BY c.fecha DESC, c.hora DESC";
        List<Cita> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Cita map(ResultSet rs) throws SQLException {
        Cita c = new Cita();
        c.setId(rs.getLong("id"));
        c.setMedicoId(rs.getLong("medico_id"));
        c.setMedicoNombre(rs.getString("medico_nombre"));
        c.setPacienteId(rs.getLong("paciente_id"));
        c.setPacienteNombre(rs.getString("paciente_nombre"));
        c.setSedeId(rs.getLong("sede_id"));
        c.setSedeNombre(rs.getString("sede_nombre"));
        c.setEspecialidad(rs.getString("especialidad"));
        c.setFecha(rs.getDate("fecha").toLocalDate());
        c.setHora(rs.getTime("hora").toLocalTime());
        c.setEstado(EstadoCita.valueOf(rs.getString("estado")));
        c.setMotivo(rs.getString("motivo"));
        c.setDiagnostico(rs.getString("diagnostico"));
        return c;
    }
}
