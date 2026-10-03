package com.sicimed.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicamentoRequest(
        Long id,
        @NotBlank(message = "El nombre del medicamento es obligatorio") String nombre,
        String descripcion,
        Boolean activo) {
}
