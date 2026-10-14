package mx.edu.utch.huertos;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "readings")
public class Reading {
  @Id public UUID id;

  @Column(nullable = false)
  public UUID parcelId;

  @Column(nullable = false)
  public Double soilMoisture;

  @Column(nullable = false)
  public Double temperature;

  @Column(nullable = false)
  public Double ph;

  @Column(nullable = false)
  public Double nitrogen;

  @Column(nullable = false)
  public Double phosphorus;

  @Column(nullable = false)
  public Double potassium;

  @Column(nullable = false)
  public Integer airQuality;

  @Column(nullable = false)
  public Instant measuredAt;

  public Reading() {}

  public static Reading from(ReadingRequest q) {
    Reading r = new Reading();
    r.id = UUID.randomUUID();
    r.parcelId = q.parcelId();
    r.soilMoisture = q.soilMoisture();
    r.temperature = q.temperature();
    r.ph = q.ph();
    r.nitrogen = q.nitrogen();
    r.phosphorus = q.phosphorus();
    r.potassium = q.potassium();
    r.airQuality = q.airQuality();
    r.measuredAt = q.measuredAt();
    return r;
  }
}
