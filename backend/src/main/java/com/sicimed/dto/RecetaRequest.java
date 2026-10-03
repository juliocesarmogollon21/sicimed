package com.sicimed.dto;

import jakarta.validation.constraints.NotBlank;

public record RecetaRequest(
        @NotBlank(message = "Las indicaciones son obligatorias") String indicaciones,
        String medicamentos) {
}
