package com.sicimed.controlador;

import com.sicimed.dto.AuthResponse;
import com.sicimed.dto.LoginRequest;
import com.sicimed.dto.RegisterRequest;
import com.sicimed.servicio.AuthServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Inicio de sesion y registro de pacientes")
@SecurityRequirements
public class AuthController {

    private final AuthServicio authServicio;

    public AuthController(AuthServicio authServicio) {
        this.authServicio = authServicio;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Devuelve un token JWT y los datos del usuario con su rol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticacion correcta"),
            @ApiResponse(responseCode = "400", description = "Credenciales invalidas")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authServicio.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar paciente", description = "Crea una cuenta con rol PACIENTE y devuelve su token")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o DNI repetido"),
            @ApiResponse(responseCode = "409", description = "Usuario ya registrado")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServicio.register(request));
    }
}
