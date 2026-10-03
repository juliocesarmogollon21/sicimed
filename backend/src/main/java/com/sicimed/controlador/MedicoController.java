package com.sicimed.controlador;

import com.sicimed.dto.MedicoRequest;
import com.sicimed.dto.MedicoResponse;
import com.sicimed.servicio.MedicoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@Tag(name = "Medicos", description = "Catalogo de medicos y agenda")
public class MedicoController {

    private final MedicoServicio medicoServicio;

    public MedicoController(MedicoServicio medicoServicio) {
        this.medicoServicio = medicoServicio;
    }

    @GetMapping
    @Operation(summary = "Listar medicos", description = "Filtros por sede y especialidad")
    public ResponseEntity<List<MedicoResponse>> listar(
            @RequestParam(required = false) Long sedeId,
            @RequestParam(required = false) Long especialidadId,
            @Parameter(description = "Incluye tambien los inactivos (solo ADMIN)")
            @RequestParam(defaultValue = "false") boolean all) {
        return ResponseEntity.ok(medicoServicio.listar(sedeId, especialidadId, all));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un medico")
    @ApiResponses(@ApiResponse(responseCode = "404", description = "Medico no encontrado"))
    public ResponseEntity<MedicoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(medicoServicio.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar un medico")
    public ResponseEntity<MedicoResponse> crear(@Valid @RequestBody MedicoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoServicio.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un medico")
    public ResponseEntity<MedicoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody MedicoRequest request) {
        return ResponseEntity.ok(medicoServicio.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar un medico", description = "Baja logica: no borra citas ya registradas")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        medicoServicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
