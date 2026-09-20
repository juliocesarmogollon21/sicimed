package com.sicimed.dao;

import com.sicimed.model.Paciente;
import com.sicimed.util.DbConnection;

import java.sql.*;
import java.util.Optional;

public class PacienteDao {

    public Optional<Paciente> findById(Long id) throws SQLException {
        String sql = """
            SELECT p.*, u.nombre_completo FROM pacientes p
            JOIN usuarios u ON u.id = p.usuario_id WHERE p.id = ?
            """;
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Optional<Paciente> findByUsuarioId(Long usuarioId) throws SQLException {
        String sql = """
            SELECT p.*, u.nombre_completo FROM pacientes p
            JOIN usuarios u ON u.id = p.usuario_id WHERE p.usuario_id = ?
            """;
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Optional<Paciente> findByDni(String dni) throws SQLException {
        String sql = """
            SELECT p.*, u.nombre_completo FROM pacientes p
            JOIN usuarios u ON u.id = p.usuario_id WHERE p.dni = ?
            """;
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Paciente insert(Paciente p) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO pacientes(usuario_id, dni, fecha_nacimiento, telefono) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getUsuarioId());
            ps.setString(2, p.getDni());
            if (p.getFechaNacimiento() != null) {
                ps.setDate(3, Date.valueOf(p.getFechaNacimiento()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, p.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                }
            }
        }
        if (p.getId() != null) {
            return findById(p.getId()).orElse(p);
        }
        return p;
    }

    private Paciente map(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId(rs.getLong("id"));
        p.setUsuarioId(rs.getLong("usuario_id"));
        p.setNombreCompleto(rs.getString("nombre_completo"));
        p.setDni(rs.getString("dni"));
        Date fn = rs.getDate("fecha_nacimiento");
        if (fn != null) p.setFechaNacimiento(fn.toLocalDate());
        p.setTelefono(rs.getString("telefono"));
        return p;
    }
}
