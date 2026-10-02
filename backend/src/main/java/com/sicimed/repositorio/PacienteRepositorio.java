package com.sicimed.repositorio;

import com.sicimed.modelo.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PacienteRepositorio extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByDni(String dni);

    boolean existsByDni(String dni);

    @Query("SELECT p FROM Paciente p WHERE p.usuario.id = :usuarioId")
    Optional<Paciente> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
