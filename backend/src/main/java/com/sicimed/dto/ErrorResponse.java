package com.sicimed.dto;

/** Cuerpo JSON uniforme para los errores de la API: {"status": 404, "message": "..."}. */
public record ErrorResponse(int status, String message) {
}