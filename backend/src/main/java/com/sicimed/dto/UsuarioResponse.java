package com.sicimed.dto;

import com.sicimed.modelo.Rol;

public record UsuarioResponse(
        Long id,
        String username,
        String nombreCompleto,
        String email,
        Rol rol,
        boolean activo) {
}
