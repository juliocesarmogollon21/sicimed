package com.sicimed.controlador;

import com.sicimed.dto.SedeRequest;
import com.sicimed.dto.SedeResponse;
import com.sicimed.servicio.SedeServicio;import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/sedes")
@Tag(name = "Sedes", description = "Sedes del policlinico")
public class SedeController {

    private final SedeServicio sedeServicio;

    public SedeController(SedeServicio sedeServicio) {
        this.sedeServicio = sedeServicio;
    }

    @GetMapping
    @Operation(summary = "Listar sedes", description = "all=true incluye las inactivas")
    public ResponseEntity<List<SedeResponse>> listar(@RequestParam(defaultValue = "false") boolean all) {
        return ResponseEntity.ok(sedeServicio.listar(all));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una sede")
    @ApiResponses(@ApiResponse(responseCode = "404", description = "Sede no encontrada"))
    public ResponseEntity<SedeResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(sedeServicio.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear sede")
    public ResponseEntity<SedeResponse> crear(@Valid @RequestBody SedeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sedeServicio.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar sede")
    public ResponseEntity<SedeResponse> actualizar(@PathVariable Long id, @Valid @RequestBody SedeRequest request) {
        return ResponseEntity.ok(sedeServicio.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar sede", description = "Baja logica")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        sedeServicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}