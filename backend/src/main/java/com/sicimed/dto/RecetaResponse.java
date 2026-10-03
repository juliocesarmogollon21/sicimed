package com.sicimed.dto;

import java.time.LocalDateTime;

public record RecetaResponse(
        Long id,
        Long citaId,
        String indicaciones,
        String medicamentos,
        LocalDateTime creadoEn) {
}
