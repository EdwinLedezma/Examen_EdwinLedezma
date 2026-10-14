package mx.edu.utch.huertos;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
  public NotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", message);
  }
}
