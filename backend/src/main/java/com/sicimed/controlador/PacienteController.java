package com.sicimed.controlador;

import com.sicimed.dto.PacienteResponse;
import com.sicimed.servicio.PacienteServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Consulta de pacientes")
public class PacienteController {

    private final PacienteServicio pacienteServicio;

    public PacienteController(PacienteServicio pacienteServicio) {
        this.pacienteServicio = pacienteServicio;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA', 'MEDICO')")
    @Operation(summary = "Buscar paciente por DNI", description = "El DNI es obligatorio")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Falta el parametro dni"),
            @ApiResponse(responseCode = "404", description = "No existe un paciente con ese DNI")
    })
    public ResponseEntity<PacienteResponse> buscar(@RequestParam(required = false) String dni) {
        return ResponseEntity.ok(pacienteServicio.buscarPorDni(dni));
    }
}