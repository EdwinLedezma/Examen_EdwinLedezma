package mx.edu.utch.huertos;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class MonitoringClient {
  private final RestClient client;
  private final String url;

  public MonitoringClient(RestClient client, @Value("${services.monitoring}") String url) {
    this.client = client;
    this.url = url;
  }

  public Object current(UUID id) {
    try {
      return client
          .get()
          .uri(url + "/api/v1/readings/parcels/{id}/current", id)
          .retrieve()
          .body(Object.class);
    } catch (RestClientException ex) {
      throw new DependencyException();
    }
  }
}
