package mx.edu.utch.huertos;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ParcelApiTest {
  @Autowired MockMvc mvc;
  @Autowired ParcelRepository repository;
  @MockitoBean MonitoringClient monitoring;
  static final String VALID =
      """
{"name":"Huerto Norte","location":{"latitude":28.63,"longitude":-106.08},"soilType":"LIMOSO","ownerOrLead":"Edwin"}
""";

  @BeforeEach
  void setup() {
    repository.deleteAll();
    when(monitoring.current(any())).thenReturn(Map.of("status", "SIN_DATOS"));
  }

  @Test
  void createsAndLists() throws Exception {
    mvc.perform(post("/api/v1/parcels").contentType(MediaType.APPLICATION_JSON).content(VALID))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.createdAt").exists());
    mvc.perform(get("/api/v1/parcels"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Huerto Norte"));
  }

  @Test
  void sanitizesBeforeValidation() throws Exception {
    mvc.perform(
            post("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("Huerto Norte", "  <b>Huerto</b>   Norte  ")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Huerto Norte"));
  }

  @Test
  void rejectsTextEmptyAfterSanitizing() throws Exception {
    mvc.perform(
            post("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("Huerto Norte", "<script>alert(1)</script>")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").exists());
  }

  @Test
  void rejectsInvalidGps() throws Exception {
    mvc.perform(
            post("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("28.63", "91")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors['location.latitude']").exists());
  }

  @Test
  void rejectsMissingLocation() throws Exception {
    mvc.perform(
            post("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("{\"latitude\":28.63,\"longitude\":-106.08}", "null")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.location").exists());
  }

  @Test
  void rejectsDuplicateNormalizedName() throws Exception {
    mvc.perform(post("/api/v1/parcels").contentType(MediaType.APPLICATION_JSON).content(VALID))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("Huerto Norte", "  HUERTO NORTE  ")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("PARCELA_DUPLICADA"));
  }

  @Test
  void rejectsBadEnumAndMalformedJson() throws Exception {
    mvc.perform(
            post("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("LIMOSO", "OTRO")))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/v1/parcels").contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("SOLICITUD_INVALIDA"));
  }

  @Test
  void rejectsClientControlledId() throws Exception {
    mvc.perform(
            post("/api/v1/parcels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID.replace("\"name\"", "\"id\":\"x\",\"name\"")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void invalidIdAndMissingParcel() throws Exception {
    mvc.perform(get("/api/v1/parcels/no-uuid")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/parcels/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("RECURSO_NO_ENCONTRADO"));
  }

  @Test
  void detailIncludesCurrentStateAndHandlesOutage() throws Exception {
    mvc.perform(post("/api/v1/parcels").contentType(MediaType.APPLICATION_JSON).content(VALID))
        .andExpect(status().isCreated());
    var id = repository.findAll().get(0).id;
    mvc.perform(get("/api/v1/parcels/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentState.status").value("SIN_DATOS"));
    when(monitoring.current(id)).thenThrow(new DependencyException());
    mvc.perform(get("/api/v1/parcels/" + id))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value("SERVICIO_NO_DISPONIBLE"));
  }
}
