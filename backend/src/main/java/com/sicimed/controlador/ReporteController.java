package com.sicimed.controlador;

import com.sicimed.dto.CitaResponse;
import com.sicimed.dto.PageResponse;
import com.sicimed.modelo.EstadoCita;
import com.sicimed.seguridad.ActualizadorAutenticacion;
import com.sicimed.servicio.CitaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")
@Tag(name = "Reportes", description = "Consultas agregadas de citas con paginacion")
public class ReporteController {

    private final CitaServicio citaServicio;
    private final ActualizadorAutenticacion autenticacion;

    public ReporteController(CitaServicio citaServicio, ActualizadorAutenticacion autenticacion) {
        this.citaServicio = citaServicio;
        this.autenticacion = autenticacion;
    }

    @GetMapping("/citas")
    @Operation(summary = "Reporte de citas", description = "Filtra por medico, estado y rango de fechas")
    public ResponseEntity<PageResponse<CitaResponse>> reporte(
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.ASC, "fecha", "hora"));
        return ResponseEntity.ok(PageResponse.from(
                citaServicio.reporte(medicoId, estado, desde, hasta, pageable), c -> c));
    }
}
