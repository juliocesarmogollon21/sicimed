package com.sicimed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(
        @NotNull(message = "Debe indicar el medico") Long medicoId,
        Long pacienteId,
        @NotNull(message = "Debe indicar la sede") Long sedeId,
        @NotNull(message = "Debe indicar la fecha") LocalDate fecha,
        @NotNull(message = "Debe indicar la hora")
    @JsonFormat(pattern = "HH:mm") LocalTime hora,
        @NotBlank(message = "Debe indicar el motivo") String motivo) {
}
