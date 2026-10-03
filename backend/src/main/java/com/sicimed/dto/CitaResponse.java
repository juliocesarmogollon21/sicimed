package com.sicimed.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sicimed.modelo.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaResponse(
        Long id,
        Long medicoId,
        String medicoNombre,
        Long pacienteId,
        String pacienteNombre,
        Long sedeId,
        String sedeNombre,
        String especialidad,
        LocalDate fecha,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        EstadoCita estado,
        String motivo,
        String diagnostico) {
}
