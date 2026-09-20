package com.sicimed.dao;

import com.sicimed.model.Especialidad;
import com.sicimed.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EspecialidadDao {

    public List<Especialidad> findAll() throws SQLException {
        List<Especialidad> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM especialidades ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Especialidad> findById(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM especialidades WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Especialidad insert(Especialidad e) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO especialidades(nombre,descripcion) VALUES (?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDescripcion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                e.setId(rs.getLong(1));
            }
        }
        return findById(e.getId()).orElse(e);
    }

    public void update(Especialidad e) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE especialidades SET nombre=?, descripcion=? WHERE id=?")) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getDescripcion());
            ps.setLong(3, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM especialidades WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Especialidad map(ResultSet rs) throws SQLException {
        Especialidad e = new Especialidad();
        e.setId(rs.getLong("id"));
        e.setNombre(rs.getString("nombre"));
        e.setDescripcion(rs.getString("descripcion"));
        return e;
    }
}
