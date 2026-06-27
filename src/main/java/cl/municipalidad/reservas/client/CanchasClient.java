package cl.municipalidad.reservas.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import cl.municipalidad.reservas.dto.response.DtoCanchaResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class CanchasClient {

    private static final Logger logger = LoggerFactory.getLogger(CanchasClient.class);
    private final WebClient webClient;

    public CanchasClient(@Qualifier("webClientCanchas") WebClient webClient) {
        this.webClient = webClient;
    }

    public DtoCanchaResponse consultarCancha(String token, Integer idCancha) {
        try {
            logger.info("Verificando Cancha ID: {} de forma interna", idCancha);
            
            return webClient.get()
                    .uri("/api/v1/canchas/" + idCancha)
                    .header("Authorization", token) 
                    .retrieve()
                    .bodyToMono(DtoCanchaResponse.class)
                    .block();
                    
        } catch (WebClientResponseException.NotFound e) {
            logger.warn("Cancha con ID {} no encontrada (404) en ms-canchas.", idCancha);
            return null;
        } catch (Exception e) {
            logger.error("Error al consultar cancha {}: {}", idCancha, e.getMessage());
            return null;
        }
    }
}