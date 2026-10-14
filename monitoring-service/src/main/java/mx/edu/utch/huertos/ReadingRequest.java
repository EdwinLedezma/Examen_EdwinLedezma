package mx.edu.utch.huertos;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public record ReadingRequest(
    @NotNull(message = "La parcela es obligatoria.") UUID parcelId,
    @NotNull(message = "La humedad es obligatoria.")
        @DecimalMin(value = "0", message = "Humedad mínima: 0 %.")
        @DecimalMax(value = "100", message = "Humedad máxima: 100 %.")
        Double soilMoisture,
    @NotNull(message = "La temperatura es obligatoria.")
        @DecimalMin(value = "-50", message = "Temperatura mínima: -50 °C.")
        @DecimalMax(value = "80", message = "Temperatura máxima: 80 °C.")
        Double temperature,
    @NotNull(message = "El pH es obligatorio.")
        @DecimalMin(value = "0", message = "pH mínimo: 0.")
        @DecimalMax(value = "14", message = "pH máximo: 14.")
        Double ph,
    @NotNull(message = "El nitrógeno es obligatorio.")
        @DecimalMin(value = "0", message = "Nitrógeno mínimo: 0.")
        @DecimalMax(value = "10000", message = "Nitrógeno máximo: 10000 mg/kg.")
        Double nitrogen,
    @NotNull(message = "El fósforo es obligatorio.")
        @DecimalMin(value = "0", message = "Fósforo mínimo: 0.")
        @DecimalMax(value = "10000", message = "Fósforo máximo: 10000 mg/kg.")
        Double phosphorus,
    @NotNull(message = "El potasio es obligatorio.")
        @DecimalMin(value = "0", message = "Potasio mínimo: 0.")
        @DecimalMax(value = "10000", message = "Potasio máximo: 10000 mg/kg.")
        Double potassium,
    @NotNull(message = "El índice de calidad del aire es obligatorio.")
        @Min(value = 0, message = "AQI mínimo: 0.")
        @Max(value = 500, message = "AQI máximo: 500.")
        Integer airQuality,
    @NotNull(message = "La fecha de medición es obligatoria.")
        @PastOrPresent(message = "La medición no puede tener fecha futura.")
        Instant measuredAt) {}
