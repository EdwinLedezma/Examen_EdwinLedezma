package mx.edu.utch.huertos;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parcels")
public class Parcel {
  @Id public UUID id;

  @Column(nullable = false, length = 100)
  public String name;

  @Column(name = "name_key", nullable = false, unique = true, length = 100)
  public String nameKey;

  @Column(nullable = false)
  public Double latitude;

  @Column(nullable = false)
  public Double longitude;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  public SoilType soilType;

  @Column(nullable = false, length = 120)
  public String ownerOrLead;

  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  public Parcel() {}
}
