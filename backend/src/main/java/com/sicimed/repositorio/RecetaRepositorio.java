package com.sicimed.repositorio;

import com.sicimed.modelo.Receta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecetaRepositorio extends JpaRepository<Receta, Long> {

    Optional<Receta> findByCitaId(Long citaId);

    boolean existsByCitaId(Long citaId);
}
