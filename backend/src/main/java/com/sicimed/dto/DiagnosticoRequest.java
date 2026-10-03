package com.sicimed.dto;

import jakarta.validation.constraints.NotBlank;

public record DiagnosticoRequest(
        @NotBlank(message = "El diagnostico es obligatorio") String diagnostico) {
}
