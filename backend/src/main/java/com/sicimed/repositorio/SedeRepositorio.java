package com.sicimed.repositorio;

import com.sicimed.modelo.Sede;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SedeRepositorio extends JpaRepository<Sede, Long> {

    List<Sede> findByActivoTrueOrderByNombre();

    List<Sede> findAllByOrderByNombre();
}
