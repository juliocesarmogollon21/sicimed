package com.sicimed.dao;

import com.sicimed.model.Sede;
import com.sicimed.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SedeDao {

    public List<Sede> findAll() throws SQLException {
        List<Sede> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM sedes ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Sede> findActivas() throws SQLException {
        List<Sede> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM sedes WHERE activo = 1 ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Sede> findById(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM sedes WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Sede insert(Sede sede) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO sedes(nombre,direccion,telefono,activo) VALUES (?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sede.getNombre());
            ps.setString(2, sede.getDireccion());
            ps.setString(3, sede.getTelefono());
            ps.setBoolean(4, sede.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                sede.setId(rs.getLong(1));
            }
        }
        return findById(sede.getId()).orElse(sede);
    }

    public void update(Sede sede) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE sedes SET nombre=?, direccion=?, telefono=?, activo=? WHERE id=?")) {
            ps.setString(1, sede.getNombre());
            ps.setString(2, sede.getDireccion());
            ps.setString(3, sede.getTelefono());
            ps.setBoolean(4, sede.isActivo());
            ps.setLong(5, sede.getId());
            ps.executeUpdate();
        }
    }

    public void softDelete(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE sedes SET activo = 0 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void activate(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE sedes SET activo = 1 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Sede map(ResultSet rs) throws SQLException {
        Sede s = new Sede();
        s.setId(rs.getLong("id"));
        s.setNombre(rs.getString("nombre"));
        s.setDireccion(rs.getString("direccion"));
        s.setTelefono(rs.getString("telefono"));
        s.setActivo(rs.getBoolean("activo"));
        return s;
    }
}
