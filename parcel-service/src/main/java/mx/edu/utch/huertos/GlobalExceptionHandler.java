package mx.edu.utch.huertos;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  public record ErrorBody(
      Instant timestamp,
      int status,
      String code,
      String message,
      String path,
      Map<String, String> errors) {}

  private ResponseEntity<ErrorBody> error(
      HttpStatus status,
      String code,
      String message,
      Map<String, String> errors,
      HttpServletRequest req) {
    return ResponseEntity.status(status)
        .body(
            new ErrorBody(
                Instant.now(), status.value(), code, message, req.getRequestURI(), errors));
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorBody> business(ApiException ex, HttpServletRequest req) {
    return error(ex.status, ex.code, ex.getMessage(), Map.of(), req);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, String> fields = new TreeMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
    return error(
        HttpStatus.BAD_REQUEST, "VALIDACION_FALLIDA", "Revisa los campos enviados.", fields, req);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ErrorBody> malformed(Exception ex, HttpServletRequest req) {
    return error(
        HttpStatus.BAD_REQUEST,
        "SOLICITUD_INVALIDA",
        "JSON, identificador, fecha o tipo de suelo inválido. Revisa el formato y los valores"
            + " permitidos.",
        Map.of(),
        req);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ErrorBody> conflict(Exception ex, HttpServletRequest req) {
    return error(
        HttpStatus.CONFLICT,
        "CONFLICTO_DATOS",
        "Los datos entran en conflicto con un registro existente.",
        Map.of(),
        req);
  }

  @ExceptionHandler(DataAccessException.class)
  ResponseEntity<ErrorBody> database(Exception ex, HttpServletRequest req) {
    LoggerFactory.getLogger(getClass()).error("Error de persistencia", ex);
    return error(
        HttpStatus.SERVICE_UNAVAILABLE,
        "BASE_NO_DISPONIBLE",
        "No fue posible acceder a los datos. Intenta más tarde.",
        Map.of(),
        req);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ErrorBody> missing(Exception ex, HttpServletRequest req) {
    return error(
        HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA", "La ruta solicitada no existe.", Map.of(), req);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ErrorBody> method(Exception ex, HttpServletRequest req) {
    return error(
        HttpStatus.METHOD_NOT_ALLOWED,
        "METODO_NO_PERMITIDO",
        "El método HTTP no está permitido en esta ruta.",
        Map.of(),
        req);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorBody> unexpected(Exception ex, HttpServletRequest req) {
    LoggerFactory.getLogger(getClass()).error("Error inesperado", ex);
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "ERROR_INTERNO",
        "Ocurrió un error inesperado. Contacta al administrador.",
        Map.of(),
        req);
  }
}
