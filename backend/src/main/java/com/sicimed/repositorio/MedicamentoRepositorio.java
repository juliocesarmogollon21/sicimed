package com.sicimed.repositorio;

import com.sicimed.modelo.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicamentoRepositorio extends JpaRepository<Medicamento, Long> {

    List<Medicamento> findByActivoTrueOrderByNombre();

    List<Medicamento> findAllByOrderByNombre();
}
