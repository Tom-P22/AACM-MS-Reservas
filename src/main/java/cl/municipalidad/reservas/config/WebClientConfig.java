package cl.municipalidad.reservas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${api.canchas.base-url}")
    private String canchasBaseUrl;

    @Bean(name = "webClientCanchas")
    public WebClient webClientCanchas() {
        return WebClient.builder()
                .baseUrl(canchasBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}   