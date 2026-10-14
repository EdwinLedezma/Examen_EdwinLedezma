package mx.edu.utch.huertos;

import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelService {
  private final ParcelRepository repository;

  public ParcelService(ParcelRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public Parcel create(ParcelRequest request) {
    String key = request.name().toLowerCase(Locale.ROOT);
    if (repository.existsByNameKey(key)) throw new DuplicateException();
    Parcel p = new Parcel();
    p.id = UUID.randomUUID();
    p.name = request.name();
    p.nameKey = key;
    p.latitude = request.location().latitude();
    p.longitude = request.location().longitude();
    p.soilType = request.soilType();
    p.ownerOrLead = request.ownerOrLead();
    p.createdAt = Instant.now();
    return repository.saveAndFlush(p);
  }

  @Transactional(readOnly = true)
  public List<Parcel> all() {
    return repository.findAll(Sort.by("createdAt").descending());
  }

  @Transactional(readOnly = true)
  public Parcel find(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("La parcela solicitada no existe."));
  }
}
