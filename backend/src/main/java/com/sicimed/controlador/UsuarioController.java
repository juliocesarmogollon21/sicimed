package com.sicimed.controlador;

import com.sicimed.dto.UsuarioRequest;
import com.sicimed.dto.UsuarioResponse;
import com.sicimed.servicio.UsuarioServicio;
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
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuarios", description = "Administracion de cuentas del sistema")
public class UsuarioController {

    private final UsuarioServicio usuarioServicio;

    public UsuarioController(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Nunca devuelve el hash de la contrasena")
    public ResponseEntity<List<UsuarioResponse>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(usuarioServicio.listar(activo));
    }

    @PostMapping
    @Operation(summary = "Crear usuario")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioServicio.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario",
            description = "La contrasena solo se cambia si se envia una nueva")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                    @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioServicio.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario", description = "Baja logica")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioServicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
