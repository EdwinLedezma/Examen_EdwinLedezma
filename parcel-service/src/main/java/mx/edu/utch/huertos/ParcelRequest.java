package mx.edu.utch.huertos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record ParcelRequest(
    @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
        String name,
    @NotNull(message = "La ubicación es obligatoria.") @Valid Location location,
    @NotNull(message = "El tipo de suelo es obligatorio: ARCILLOSO, LIMOSO o ARENOSO.")
        SoilType soilType,
    @NotBlank(message = "El responsable es obligatorio.")
        @Size(max = 120, message = "El responsable admite hasta 120 caracteres.")
        String ownerOrLead) {
  public ParcelRequest {
    name = TextSanitizer.clean(name);
    ownerOrLead = TextSanitizer.clean(ownerOrLead);
  }

  public record Location(
      @NotNull(message = "La latitud es obligatoria.")
          @DecimalMin(value = "-90", message = "Latitud mínima: -90.")
          @DecimalMax(value = "90", message = "Latitud máxima: 90.")
          Double latitude,
      @NotNull(message = "La longitud es obligatoria.")
          @DecimalMin(value = "-180", message = "Longitud mínima: -180.")
          @DecimalMax(value = "180", message = "Longitud máxima: 180.")
          Double longitude) {}
}
