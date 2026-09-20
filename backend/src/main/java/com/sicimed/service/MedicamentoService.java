package com.sicimed.service;

import com.sicimed.dao.MedicamentoDao;
import com.sicimed.model.Medicamento;

import java.sql.SQLException;
import java.util.List;

public class MedicamentoService {

    private final MedicamentoDao dao = new MedicamentoDao();

    public List<Medicamento> listar(boolean all) throws SQLException {
        return all ? dao.findAll() : dao.findActivos();
    }

    public Medicamento obtener(Long id) throws SQLException {
        return dao.findById(id).orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado"));
    }

    public Medicamento crear(String nombre, String descripcion, boolean activo) throws SQLException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        Medicamento m = new Medicamento();
        m.setNombre(nombre.trim());
        m.setDescripcion(descripcion);
        m.setActivo(activo);
        return dao.insert(m);
    }

    public Medicamento actualizar(Long id, String nombre, String descripcion, Boolean activo) throws SQLException {
        Medicamento m = obtener(id);
        if (nombre != null && !nombre.isBlank()) m.setNombre(nombre.trim());
        if (descripcion != null) m.setDescripcion(descripcion);
        if (activo != null) m.setActivo(activo);
        dao.update(m);
        return obtener(id);
    }

    public void desactivar(Long id) throws SQLException {
        obtener(id);
        dao.softDelete(id);
    }
}
