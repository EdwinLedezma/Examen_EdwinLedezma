package mx.edu.utch.huertos;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MonitoringApiTest {
  @Autowired MockMvc mvc;
  @Autowired ReadingRepository repository;
  @MockitoBean ParcelClient parcels;
  final UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");

  String valid() {
    return """
{"parcelId":"11111111-1111-1111-1111-111111111111","soilMoisture":50,"temperature":25,"ph":6.5,"nitrogen":40,"phosphorus":20,"potassium":100,"airQuality":50,"measuredAt":"2026-01-01T12:00:00Z"}
""";
  }

  @BeforeEach
  void setup() {
    repository.deleteAll();
  }

  @Test
  void noDataIsExplicit() throws Exception {
    mvc.perform(get("/api/v1/readings/parcels/" + id + "/current"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SIN_DATOS"));
  }

  @Test
  void savesNormalReading() throws Exception {
    mvc.perform(post("/api/v1/readings").contentType(MediaType.APPLICATION_JSON).content(valid()))
        .andExpect(status().isCreated());
    verify(parcels).requireExists(id);
    mvc.perform(get("/api/v1/readings/parcels/" + id + "/current"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("NORMAL"));
  }

  @Test
  void alertsAndUsesMeasurementTime() throws Exception {
    mvc.perform(
            post("/api/v1/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    valid()
                        .replace("2026-01-01", "2026-01-02")
                        .replace("\"soilMoisture\":50", "\"soilMoisture\":10")))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/v1/readings").contentType(MediaType.APPLICATION_JSON).content(valid()))
        .andExpect(status().isCreated());
    mvc.perform(get("/api/v1/readings/parcels/" + id + "/current"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ALERTA"))
        .andExpect(jsonPath("$.latestReading.soilMoisture").value(10))
        .andExpect(jsonPath("$.recommendations[0]").exists());
  }

  @Test
  void rejectsOutOfRangeAndFutureDate() throws Exception {
    mvc.perform(
            post("/api/v1/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    valid().replace("\"ph\":6.5", "\"ph\":15").replace("2026-01-01", "2099-01-01")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.ph").exists())
        .andExpect(jsonPath("$.errors.measuredAt").exists());
    verifyNoInteractions(parcels);
  }

  @Test
  void missingParcelDoesNotPersist() throws Exception {
    doThrow(new NotFoundException("La parcela no existe.")).when(parcels).requireExists(id);
    mvc.perform(post("/api/v1/readings").contentType(MediaType.APPLICATION_JSON).content(valid()))
        .andExpect(status().isNotFound());
    Assertions.assertEquals(0, repository.count());
  }

  @Test
  void dependencyOutageDoesNotPersist() throws Exception {
    doThrow(new DependencyException()).when(parcels).requireExists(id);
    mvc.perform(post("/api/v1/readings").contentType(MediaType.APPLICATION_JSON).content(valid()))
        .andExpect(status().isServiceUnavailable());
    Assertions.assertEquals(0, repository.count());
  }
}
