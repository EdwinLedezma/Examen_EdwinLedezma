package mx.edu.utch.huertos;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class ParcelClient {
  private final RestClient client;
  private final String url;

  public ParcelClient(RestClient client, @Value("${services.parcels}") String url) {
    this.client = client;
    this.url = url;
  }

  public void requireExists(UUID id) {
    try {
      client.get().uri(url + "/api/v1/parcels/{id}/exists", id).retrieve().toBodilessEntity();
    } catch (HttpClientErrorException.NotFound ex) {
      throw new NotFoundException("No se puede registrar la lectura: la parcela no existe.");
    } catch (RestClientException ex) {
      throw new DependencyException();
    }
  }
}
