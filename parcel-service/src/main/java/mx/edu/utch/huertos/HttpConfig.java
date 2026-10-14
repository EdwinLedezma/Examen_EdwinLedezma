package mx.edu.utch.huertos;

import java.time.Duration;
import org.springframework.context.annotation.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpConfig {
  @Bean
  RestClient restClient() {
    var f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(Duration.ofSeconds(2));
    f.setReadTimeout(Duration.ofSeconds(3));
    return RestClient.builder().requestFactory(f).build();
  }
}
