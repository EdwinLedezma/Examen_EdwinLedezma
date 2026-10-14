package mx.edu.utch.huertos;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelRepository extends JpaRepository<Parcel, UUID> {
  boolean existsByNameKey(String key);
}
