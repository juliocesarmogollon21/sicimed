package com.sicimed.controlador;

import com.sicimed.dto.MedicamentoRequest;
import com.sicimed.dto.MedicamentoResponse;
import com.sicimed.servicio.MedicamentoServicio;import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/medicamentos")
@Tag(name = "Medicamentos", description = "Catalogo de medicamentos para recetas")
public class MedicamentoController {

    private final MedicamentoServicio medicamentoServicio;

    public MedicamentoController(MedicamentoServicio medicamentoServicio) {
        this.medicamentoServicio = medicamentoServicio;
    }

    @GetMapping
    @Operation(summary = "Listar medicamentos", description = "all=true incluye los inactivos")
    public ResponseEntity<List<MedicamentoResponse>> listar(@RequestParam(defaultValue = "false") boolean all) {
        return ResponseEntity.ok(medicamentoServicio.listar(all));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un medicamento")
    @ApiResponses(@ApiResponse(responseCode = "404", description = "Medicamento no encontrado"))
    public ResponseEntity<MedicamentoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentoServicio.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear medicamento")
    public ResponseEntity<MedicamentoResponse> crear(@Valid @RequestBody MedicamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicamentoServicio.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar medicamento")
    public ResponseEntity<MedicamentoResponse> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody MedicamentoRequest request) {
        return ResponseEntity.ok(medicamentoServicio.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar medicamento", description = "Baja logica")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        medicamentoServicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}