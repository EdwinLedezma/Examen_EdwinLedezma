package mx.edu.utch.huertos;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parcels")
public class ParcelController {
  private final ParcelService service;
  private final MonitoringClient monitoring;

  public ParcelController(ParcelService service, MonitoringClient monitoring) {
    this.service = service;
    this.monitoring = monitoring;
  }

  public record View(
      UUID id,
      String name,
      ParcelRequest.Location location,
      SoilType soilType,
      String ownerOrLead,
      Instant createdAt) {
    static View from(Parcel p) {
      return new View(
          p.id,
          p.name,
          new ParcelRequest.Location(p.latitude, p.longitude),
          p.soilType,
          p.ownerOrLead,
          p.createdAt);
    }
  }

  public record Detail(View parcel, Object currentState) {}

  @PostMapping
  public ResponseEntity<View> create(@Valid @RequestBody ParcelRequest request) {
    var p = service.create(request);
    return ResponseEntity.created(URI.create("/api/v1/parcels/" + p.id)).body(View.from(p));
  }

  @GetMapping
  public List<View> all() {
    return service.all().stream().map(View::from).toList();
  }

  @GetMapping("/{id}")
  public Detail detail(@PathVariable UUID id) {
    var p = service.find(id);
    return new Detail(View.from(p), monitoring.current(id));
  }

  // Consulta técnica para evitar llamadas circulares entre microservicios.
  @GetMapping("/{id}/exists")
  public Map<String, Boolean> exists(@PathVariable UUID id) {
    service.find(id);
    return Map.of("exists", true);
  }
}
