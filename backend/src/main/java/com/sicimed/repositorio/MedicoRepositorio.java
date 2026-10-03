package com.sicimed.repositorio;

import com.sicimed.modelo.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicoRepositorio extends JpaRepository<Medico, Long> {

    @Query("""
            SELECT m FROM Medico m
            WHERE (:sedeId IS NULL OR m.sede.id = :sedeId)
              AND (:especialidadId IS NULL OR m.especialidad.id = :especialidadId)
              AND (:incluirInactivos = true OR m.activo = true)
            ORDER BY m.usuario.nombreCompleto
            """)
    List<Medico> buscarConFiltros(@Param("sedeId") Long sedeId,
                                  @Param("especialidadId") Long especialidadId,
                                  @Param("incluirInactivos") boolean incluirInactivos);

    @Query("SELECT m FROM Medico m WHERE m.usuario.id = :usuarioId")
    Optional<Medico> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByEspecialidadId(Long especialidadId);
}
