package com.sicimed.dto;

import com.sicimed.modelo.Rol;

public record AuthResponse(
        String token,
        String username,
        String nombreCompleto,
        Rol rol,
        Long userId,
        Long medicoId,
        Long pacienteId) {
}
