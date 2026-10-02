package com.sicimed.dto;

import jakarta.validation.constraints.NotBlank;

public record SedeRequest(
        Long id,
        @NotBlank(message = "El nombre de la sede es obligatorio") String nombre,
        String direccion,
        String telefono,
        Boolean activo) {
}
