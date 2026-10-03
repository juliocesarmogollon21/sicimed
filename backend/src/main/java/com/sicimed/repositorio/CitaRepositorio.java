package com.sicimed.repositorio;

import com.sicimed.modelo.Cita;
import com.sicimed.modelo.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepositorio extends JpaRepository<Cita, Long>,
        JpaSpecificationExecutor<Cita> {

    @Query("""
            SELECT COUNT(c) FROM Cita c
            WHERE c.medico.id = :medicoId
              AND c.fecha = :fecha
              AND c.hora = :hora
              AND c.estado <> :estadoExcluido
              AND (:excludeId IS NULL OR c.id <> :excludeId)
            """)
    long contarConflictos(@Param("medicoId") Long medicoId,
                         @Param("fecha") LocalDate fecha,
                         @Param("hora") LocalTime hora,
                         @Param("estadoExcluido") EstadoCita estadoExcluido,
                         @Param("excludeId") Long excludeId);

    /** Resuelta por la Named Query "Cita.findHorasOcupadas" declarada en la entidad Cita (S5). */
    List<LocalTime> findHorasOcupadas(@Param("medicoId") Long medicoId,
                                      @Param("fecha") LocalDate fecha,
                                      @Param("cancelado") EstadoCita cancelado);

    @Query("""
            SELECT c FROM Cita c
            WHERE (:medicoId IS NULL OR c.medico.id = :medicoId)
              AND (:estado IS NULL OR c.estado = :estado)
              AND (:desde IS NULL OR c.fecha >= :desde)
              AND (:hasta IS NULL OR c.fecha <= :hasta)
            """)
    Page<Cita> findPaginadoConFiltros(@Param("medicoId") Long medicoId,
                                      @Param("estado") EstadoCita estado,
                                      @Param("desde") LocalDate desde,
                                      @Param("hasta") LocalDate hasta,
                                      Pageable pageable);
}
