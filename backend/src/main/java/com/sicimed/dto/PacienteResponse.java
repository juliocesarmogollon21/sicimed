package com.sicimed.dto;

import java.time.LocalDate;

public record PacienteResponse(
        Long id,
        Long usuarioId,
        String nombreCompleto,
        String dni,
        LocalDate fechaNacimiento,
        String telefono) {
}
