package com.sicimed.dao;

import com.sicimed.model.Receta;
import com.sicimed.util.DbConnection;

import java.sql.*;
import java.util.Optional;

public class RecetaDao {

    public Optional<Receta> findByCitaId(Long citaId) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM recetas WHERE cita_id = ?")) {
            ps.setLong(1, citaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Receta save(Receta receta) throws SQLException {
        Optional<Receta> existing = findByCitaId(receta.getCitaId());
        if (existing.isPresent()) {
            try (Connection c = DbConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE recetas SET indicaciones=?, medicamentos=? WHERE cita_id=?")) {
                ps.setString(1, receta.getIndicaciones());
                ps.setString(2, receta.getMedicamentos());
                ps.setLong(3, receta.getCitaId());
                ps.executeUpdate();
            }
            return findByCitaId(receta.getCitaId()).orElse(receta);
        }
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO recetas(cita_id,indicaciones,medicamentos) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, receta.getCitaId());
            ps.setString(2, receta.getIndicaciones());
            ps.setString(3, receta.getMedicamentos());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                receta.setId(rs.getLong(1));
            }
        }
        return findByCitaId(receta.getCitaId()).orElse(receta);
    }

    private Receta map(ResultSet rs) throws SQLException {
        Receta r = new Receta();
        r.setId(rs.getLong("id"));
        r.setCitaId(rs.getLong("cita_id"));
        r.setIndicaciones(rs.getString("indicaciones"));
        r.setMedicamentos(rs.getString("medicamentos"));
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) r.setCreadoEn(ts.toLocalDateTime());
        return r;
    }
}
