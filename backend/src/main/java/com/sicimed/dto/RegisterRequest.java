package com.sicimed.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") String password,
        @NotBlank(message = "El nombre completo es obligatorio") String nombreCompleto,
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido") String email,
        @NotBlank(message = "El DNI es obligatorio") String dni,
        String telefono) {
}
