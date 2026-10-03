package com.sicimed.dto;

public record MedicoResponse(
        Long id,
        Long usuarioId,
        String nombreCompleto,
        Long especialidadId,
        String especialidadNombre,
        Long sedeId,
        String sedeNombre,
        String cmp,
        boolean activo) {
}
