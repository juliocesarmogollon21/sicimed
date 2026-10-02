package com.sicimed.dto;

import jakarta.validation.constraints.NotNull;

public record MedicoRequest(
        @NotNull(message = "Debe indicar el usuario") Long usuarioId,
        @NotNull(message = "Debe indicar la especialidad") Long especialidadId,
        @NotNull(message = "Debe indicar la sede") Long sedeId,
        String cmp,
        Boolean activo) {
}
