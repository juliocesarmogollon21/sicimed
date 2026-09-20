package com.sicimed.dao;

import com.sicimed.model.Rol;
import com.sicimed.model.Usuario;
import com.sicimed.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDao {

    public List<Usuario> findAll() throws SQLException {
        List<Usuario> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM usuarios ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Usuario> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Optional<Usuario> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    public Usuario insert(Usuario u) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO usuarios(username,password,nombre_completo,email,rol,activo) VALUES (?,?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getNombreCompleto());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getRol().name());
            ps.setBoolean(6, u.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                u.setId(rs.getLong(1));
            }
        }
        return findById(u.getId()).orElse(u);
    }

    public void update(Usuario u) throws SQLException {
        boolean updatePass = u.getPassword() != null && !u.getPassword().isBlank();
        String sql = updatePass
                ? "UPDATE usuarios SET username=?, password=?, nombre_completo=?, email=?, rol=?, activo=? WHERE id=?"
                : "UPDATE usuarios SET username=?, nombre_completo=?, email=?, rol=?, activo=? WHERE id=?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getUsername());
            if (updatePass) ps.setString(i++, u.getPassword());
            ps.setString(i++, u.getNombreCompleto());
            ps.setString(i++, u.getEmail());
            ps.setString(i++, u.getRol().name());
            ps.setBoolean(i++, u.isActivo());
            ps.setLong(i, u.getId());
            ps.executeUpdate();
        }
    }

    public void softDelete(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET activo = 0 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    public void activate(Long id) throws SQLException {
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET activo = 1 WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Usuario map(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setEmail(rs.getString("email"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}
