package com.sicimed.controlador;

import com.sicimed.dto.EspecialidadRequest;
import com.sicimed.dto.EspecialidadResponse;
import com.sicimed.dto.MedicamentoRequest;
import com.sicimed.dto.MedicamentoResponse;
import com.sicimed.dto.SedeRequest;
import com.sicimed.dto.SedeResponse;
import com.sicimed.servicio.CatalogoServicio;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api")
@Tag(name = "Catalogos", description = "Sedes, especialidades y medicamentos")
public class CatalogoController {

    private final CatalogoServicio catalogoServicio;

    public CatalogoController(CatalogoServicio catalogoServicio) {
        this.catalogoServicio = catalogoServicio;
    }

    @GetMapping("/sedes")
    @Operation(summary = "Listar sedes")
    public ResponseEntity<List<SedeResponse>> listarSedes(
            @RequestParam(defaultValue = "false") boolean all) {
        return ResponseEntity.ok(catalogoServicio.listarSedes(all));
    }

    @PostMapping("/sedes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear sede")
    public ResponseEntity<SedeResponse> crearSede(@Valid @RequestBody SedeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoServicio.crearSede(request));
    }

    @PutMapping("/sedes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar sede")
    public ResponseEntity<SedeResponse> actualizarSede(@PathVariable Long id, @Valid @RequestBody SedeRequest request) {
        return ResponseEntity.ok(catalogoServicio.actualizarSede(id, request));
    }

    @DeleteMapping("/sedes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar sede", description = "Baja logica")
    public ResponseEntity<Void> desactivarSede(@PathVariable Long id) {
        catalogoServicio.desactivarSede(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/especialidades")
    @Operation(summary = "Listar especialidades")
    public ResponseEntity<List<EspecialidadResponse>> listarEspecialidades() {
        return ResponseEntity.ok(catalogoServicio.listarEspecialidades());
    }

    @PostMapping("/especialidades")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear especialidad")
    public ResponseEntity<EspecialidadResponse> crearEspecialidad(
            @Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoServicio.crearEspecialidad(request));
    }

    @PutMapping("/especialidades/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar especialidad")
    public ResponseEntity<EspecialidadResponse> actualizarEspecialidad(
            @PathVariable Long id, @Valid @RequestBody EspecialidadRequest request) {
        return ResponseEntity.ok(catalogoServicio.actualizarEspecialidad(id, request));
    }

    @DeleteMapping("/especialidades/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar especialidad")
    public ResponseEntity<Void> eliminarEspecialidad(@PathVariable Long id) {
        catalogoServicio.eliminarEspecialidad(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medicamentos")
    @Operation(summary = "Listar medicamentos")
    public ResponseEntity<List<MedicamentoResponse>> listarMedicamentos(
            @RequestParam(defaultValue = "false") boolean all) {
        return ResponseEntity.ok(catalogoServicio.listarMedicamentos(all));
    }

    @PostMapping("/medicamentos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear medicamento")
    public ResponseEntity<MedicamentoResponse> crearMedicamento(
            @Valid @RequestBody MedicamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoServicio.crearMedicamento(request));
    }

    @PutMapping("/medicamentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar medicamento")
    public ResponseEntity<MedicamentoResponse> actualizarMedicamento(
            @PathVariable Long id, @Valid @RequestBody MedicamentoRequest request) {
        return ResponseEntity.ok(catalogoServicio.actualizarMedicamento(id, request));
    }

    @DeleteMapping("/medicamentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar medicamento", description = "Baja logica")
    public ResponseEntity<Void> desactivarMedicamento(@PathVariable Long id) {
        catalogoServicio.desactivarMedicamento(id);
        return ResponseEntity.noContent().build();
    }
}
