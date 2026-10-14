package mx.edu.utch.huertos;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingService {
  private final ReadingRepository repository;
  private final ParcelClient parcels;
  private final RecommendationEngine engine;

  public ReadingService(
      ReadingRepository repository, ParcelClient parcels, RecommendationEngine engine) {
    this.repository = repository;
    this.parcels = parcels;
    this.engine = engine;
  }

  public Reading create(ReadingRequest request) {
    parcels.requireExists(request.parcelId());
    return repository.saveAndFlush(Reading.from(request));
  }

  @Transactional(readOnly = true)
  public RecommendationEngine.State current(UUID id) {
    return engine.evaluate(
        repository.findFirstByParcelIdOrderByMeasuredAtDescIdDesc(id).orElse(null));
  }
}
