package mx.edu.utch.huertos;

import org.springframework.http.HttpStatus;

public class DependencyException extends ApiException {
  public DependencyException() {
    super(
        HttpStatus.SERVICE_UNAVAILABLE,
        "SERVICIO_NO_DISPONIBLE",
        "El servicio requerido no está disponible. Intenta más tarde.");
  }
}
