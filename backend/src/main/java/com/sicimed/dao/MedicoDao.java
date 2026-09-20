package com.sicimed.dao;

import com.sicimed.model.Medico;
import com.sicimed.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicoDao {

    private static final String BASE = """
        SELECT m.id, m.usuario_id, u.nombre_completo, m.especialidad_id, e.nombre AS especialidad_nombre,
               m.sede_id, s.nombre AS sede_nombre, m.cmp, m.activo
        FROM medicos m
        JOIN usuarios u ON u.id = m.usuario_id
        JOIN especialidades e ON e.id = m.especialidad_id
        JOIN sedes s ON s.id = m.sede_id
        """;

    public List<Medico> findAll() throws SQLException {
        List<Medico> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + " ORDER BY m.id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Medico> findFiltrado(Long sedeId, Long especialidadId) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE + " WHERE m.activo = 1");
        List<Object> params = new ArrayList<>();
        if (sedeId != null) {
            sql.append(" AND m.sede_id = ?");
            params.add(sedeId);
        }
        if (especialidadId != null) {
            sql.append(" AND m.especialidad_id = ?");
            params.add(especialidadId);
        }
        sql.append(" ORDER BY m.id");
        List<Medico> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Medico> findById(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + " WHERE m.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Optional<Medico> findByUsuarioId(Long usuarioId) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + " WHERE m.usuario_id = ?")) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Medico insert(Medico m) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO medicos(usuario_id,especialidad_id,sede_id,cmp,activo) VALUES (?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, m.getUsuarioId());
            ps.setLong(2, m.getEspecialidadId());
            ps.setLong(3, m.getSedeId());
            ps.setString(4, m.getCmp());
            ps.setBoolean(5, m.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                m.setId(rs.getLong(1));
            }
        }
        return findById(m.getId()).orElse(m);
    }

    public void update(Medico m) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE medicos SET usuario_id=?, especialidad_id=?, sede_id=?, cmp=?, activo=? WHERE id=?")) {
            ps.setLong(1, m.getUsuarioId());
            ps.setLong(2, m.getEspecialidadId());
            ps.setLong(3, m.getSedeId());
            ps.setString(4, m.getCmp());
            ps.setBoolean(5, m.isActivo());
            ps.setLong(6, m.getId());
            ps.executeUpdate();
        }
    }

    public void softDelete(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE medicos SET activo = 0 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void activate(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE medicos SET activo = 1 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Medico map(ResultSet rs) throws SQLException {
        Medico m = new Medico();
        m.setId(rs.getLong("id"));
        m.setUsuarioId(rs.getLong("usuario_id"));
        m.setNombreCompleto(rs.getString("nombre_completo"));
        m.setEspecialidadId(rs.getLong("especialidad_id"));
        m.setEspecialidadNombre(rs.getString("especialidad_nombre"));
        m.setSedeId(rs.getLong("sede_id"));
        m.setSedeNombre(rs.getString("sede_nombre"));
        m.setCmp(rs.getString("cmp"));
        m.setActivo(rs.getBoolean("activo"));
        return m;
    }
}
