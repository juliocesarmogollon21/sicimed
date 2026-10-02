package com.sicimed.controlador;

import com.sicimed.dto.CitaRequest;
import com.sicimed.dto.CitaResponse;
import com.sicimed.dto.DiagnosticoRequest;
import com.sicimed.dto.PageResponse;
import com.sicimed.dto.RecetaRequest;
import com.sicimed.dto.RecetaResponse;
import com.sicimed.modelo.EstadoCita;
import com.sicimed.seguridad.ActualizadorAutenticacion;
import com.sicimed.seguridad.SicimedPrincipal;
import com.sicimed.servicio.CitaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Agendamiento, consulta, reprogramacion, diagnostico y receta")
public class CitaController {

    private static final int MAX_POR_PAGINA = 200;

    private final CitaServicio citaServicio;
    private final ActualizadorAutenticacion autenticacion;

    public CitaController(CitaServicio citaServicio, ActualizadorAutenticacion autenticacion) {
        this.citaServicio = citaServicio;
        this.autenticacion = autenticacion;
    }

    @GetMapping
    @Operation(summary = "Listar citas (paginado)",
            description = "El alcance depende del rol: el paciente ve las suyas, el medico su agenda, "
                    + "recepcion y admin todas o filtradas por DNI")
    public ResponseEntity<PageResponse<CitaResponse>> listar(
            @Parameter(description = "Filtra por DNI (recepcion, admin y medico)")
            @RequestParam(required = false) String dni,
            @Parameter(description = "Filtra por medico; el rol MEDICO siempre queda acotado a su agenda")
            @RequestParam(required = false) Long medicoId,
            @Parameter(description = "Filtra por fecha exacta (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean descendente) {

        SicimedPrincipal principal = autenticacion.actual();
        int limite = Math.min(Math.max(size, 1), MAX_POR_PAGINA);
        Sort.Direction direccion = descendente ? Sort.Direction.DESC : Sort.Direction.ASC;

        PageRequest pageable = PageRequest.of(Math.max(page, 0), limite,
                Sort.by(direccion, "fecha").and(Sort.by(direccion, "hora")));
        return ResponseEntity.ok(PageResponse.from(
                citaServicio.listarParaUsuario(principal, dni, medicoId, fecha, pageable), c -> c));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una cita")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "La cita no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    })
    public ResponseEntity<CitaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(citaServicio.obtenerParaUsuario(autenticacion.actual(), id));
    }

    @GetMapping("/disponibilidad")
    @Operation(summary = "Horas libres de un medico en una fecha",
            description = "Turnos de 08:00 a 17:00 en bloques de 30 minutos, sin las citas ya ocupadas")
    public ResponseEntity<List<String>> disponibilidad(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaServicio.disponibilidad(medicoId, fecha));
    }

    @PostMapping
    @Operation(summary = "Agendar una cita")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cita creada"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos o fecha pasada"),
            @ApiResponse(responseCode = "409", description = "El horario ya esta ocupado")
    })
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citaServicio.crear(autenticacion.actual(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reprogramar una cita",
            description = "Solo aplica a citas PENDIENTE; vuelve a validar el conflicto de horario")
    public ResponseEntity<CitaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody CitaRequest request) {
        return ResponseEntity.ok(citaServicio.reprogramar(autenticacion.actual(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar una cita", description = "Cambia el estado a CANCELADO y libera el horario")
    public ResponseEntity<CitaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(citaServicio.cancelar(autenticacion.actual(), id));
    }

    @PostMapping("/{id}/diagnostico")
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
    @Operation(summary = "Registrar diagnostico", description = "Pasa la cita a ATENDIDO. Solo el medico de la cita o el admin")
    public ResponseEntity<CitaResponse> diagnostico(@PathVariable Long id,
                                                    @Valid @RequestBody DiagnosticoRequest request) {
        return ResponseEntity.ok(
                citaServicio.registrarDiagnostico(autenticacion.actual(), id, request.diagnostico()));
    }

    @PostMapping("/{id}/receta")
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
    @Operation(summary = "Emitir receta", description = "Guarda o actualiza la receta asociada a la cita")
    public ResponseEntity<RecetaResponse> guardarReceta(@PathVariable Long id,
                                                       @Valid @RequestBody RecetaRequest request) {
        return ResponseEntity.ok(citaServicio.guardarReceta(autenticacion.actual(), id, request));
    }

    @GetMapping("/{id}/receta")
    @Operation(summary = "Ver la receta de una cita",
            description = "El paciente solo puede verla cuando la cita esta ATENDIDO")
    public ResponseEntity<RecetaResponse> obtenerReceta(@PathVariable Long id) {
        return ResponseEntity.ok(citaServicio.obtenerRecetaParaUsuario(autenticacion.actual(), id));
    }
}
