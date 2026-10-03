package com.sicimed.dto;

import com.sicimed.modelo.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        Long id,
        @NotBlank(message = "El usuario es obligatorio") String username,
        @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") String password,
        @NotBlank(message = "El nombre completo es obligatorio") String nombreCompleto,
        @Email(message = "El email no tiene un formato valido") String email,
        @NotNull(message = "Debe indicar el rol") Rol rol,
        Boolean activo) {
}
