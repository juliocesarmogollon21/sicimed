package com.sicimed.repositorio;

import com.sicimed.modelo.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspecialidadRepositorio extends JpaRepository<Especialidad, Long> {

    List<Especialidad> findAllByOrderByNombre();
}
