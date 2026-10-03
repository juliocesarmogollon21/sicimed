package com.sicimed.excepcion;

import com.sicimed.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException e) {
        return construir(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException e) {
        return construir(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccesoDenegadoException e) {
        return construir(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> springAccesoDenegado(AccessDeniedException e) {
        return construir(HttpStatus.FORBIDDEN, "Acceso denegado");
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> conflicto(ConflictoException e) {
        return construir(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construir(HttpStatus.BAD_REQUEST, mensaje.isBlank() ? "Datos invalidos" : mensaje);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> violacion(ConstraintViolationException e) {
        return construir(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> cuerpoInvalido(Exception e) {
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo o un parametro de la peticion no es valido");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaNoEncontrada(NoResourceFoundException e) {
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado");
    }

    /** Registro duplicado o en uso por otra tabla (FK/UNIQUE): respuesta 409 sin detalles de la BD. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException e) {
        log.warn("Violacion de integridad de datos: {}", e.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT,
                "La operacion no se pudo completar: el dato esta duplicado o esta en uso");
    }

    /** Dos usuarios editaron el mismo registro a la vez (@Version en Cita). */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> concurrencia(OptimisticLockingFailureException e) {
        return construir(HttpStatus.CONFLICT,
                "El registro fue modificado por otro usuario. Recarga e intenta de nuevo");
    }

    /**
     * Error no previsto: se registra en el log y al cliente se le envia un mensaje generico,
     * sin exponer detalles internos (S1: vulnerabilidades / fuga de informacion).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception e) {
        log.error("Error no controlado", e);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), mensaje));
    }
}
