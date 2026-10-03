package com.sicimed.controlador;

import com.sicimed.dto.EspecialidadRequest;
import com.sicimed.dto.EspecialidadResponse;
import com.sicimed.servicio.EspecialidadServicio;import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/especialidades")
@Tag(name = "Especialidades", description = "Especialidades medicas")
public class EspecialidadController {

    private final EspecialidadServicio especialidadServicio;

    public EspecialidadController(EspecialidadServicio especialidadServicio) {
        this.especialidadServicio = especialidadServicio;
    }

    @GetMapping
    @Operation(summary = "Listar especialidades")
    public ResponseEntity<List<EspecialidadResponse>> listar() {
        return ResponseEntity.ok(especialidadServicio.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una especialidad")
    @ApiResponses(@ApiResponse(responseCode = "404", description = "Especialidad no encontrada"))
    public ResponseEntity<EspecialidadResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadServicio.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear especialidad")
    @ApiResponses(@ApiResponse(responseCode = "409", description = "Nombre duplicado"))
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadServicio.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar especialidad")
    @ApiResponses(@ApiResponse(responseCode = "409", description = "Nombre duplicado"))
    public ResponseEntity<EspecialidadResponse> actualizar(@PathVariable Long id,
                                                           @Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.ok(especialidadServicio.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar especialidad")
    @ApiResponses(@ApiResponse(responseCode = "409", description = "Tiene medicos asociados"))
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especialidadServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}