package mx.edu.utch.huertos;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/readings")
public class ReadingController {
  private final ReadingService service;

  public ReadingController(ReadingService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<Reading> create(@Valid @RequestBody ReadingRequest request) {
    Reading r = service.create(request);
    return ResponseEntity.created(URI.create("/api/v1/readings/parcels/" + r.parcelId + "/current"))
        .body(r);
  }

  @GetMapping("/parcels/{id}/current")
  public RecommendationEngine.State current(@PathVariable UUID id) {
    return service.current(id);
  }
}
