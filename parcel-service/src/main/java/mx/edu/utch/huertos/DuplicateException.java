package mx.edu.utch.huertos;

import org.springframework.http.HttpStatus;

public class DuplicateException extends ApiException {
  public DuplicateException() {
    super(HttpStatus.CONFLICT, "PARCELA_DUPLICADA", "Ya existe una parcela con ese nombre.");
  }
}
