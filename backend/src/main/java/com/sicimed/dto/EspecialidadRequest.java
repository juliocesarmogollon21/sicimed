package com.sicimed.dto;

import jakarta.validation.constraints.NotBlank;

public record EspecialidadRequest(
        Long id,
        @NotBlank(message = "El nombre de la especialidad es obligatorio") String nombre,
        String descripcion) {
}
