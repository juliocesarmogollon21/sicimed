package com.sicimed.dao;

import com.sicimed.model.Medicamento;
import com.sicimed.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MedicamentoDao {

    public List<Medicamento> findAll() throws SQLException {
        List<Medicamento> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM medicamentos ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Medicamento> findActivos() throws SQLException {
        List<Medicamento> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM medicamentos WHERE activo = 1 ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Medicamento> findById(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM medicamentos WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Medicamento insert(Medicamento m) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO medicamentos(nombre,descripcion,activo) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNombre());
            ps.setString(2, m.getDescripcion());
            ps.setBoolean(3, m.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                m.setId(rs.getLong(1));
            }
        }
        return findById(m.getId()).orElse(m);
    }

    public void update(Medicamento m) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE medicamentos SET nombre=?, descripcion=?, activo=? WHERE id=?")) {
            ps.setString(1, m.getNombre());
            ps.setString(2, m.getDescripcion());
            ps.setBoolean(3, m.isActivo());
            ps.setLong(4, m.getId());
            ps.executeUpdate();
        }
    }

    public void softDelete(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE medicamentos SET activo = 0 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void activate(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE medicamentos SET activo = 1 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Medicamento map(ResultSet rs) throws SQLException {
        Medicamento m = new Medicamento();
        m.setId(rs.getLong("id"));
        m.setNombre(rs.getString("nombre"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setActivo(rs.getBoolean("activo"));
        return m;
    }
}
