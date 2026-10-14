package mx.edu.utch.huertos;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, UUID> {
  Optional<Reading> findFirstByParcelIdOrderByMeasuredAtDescIdDesc(UUID parcelId);
}
