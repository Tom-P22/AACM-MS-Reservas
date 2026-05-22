package cl.municipalidad.reservas.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import cl.municipalidad.reservas.dto.response.DtoCanchaResponse;

@Component
public class CanchasClient {

    private static final Logger logger = LoggerFactory.getLogger(CanchasClient.class);
    private final RestClient restClient;

    public CanchasClient() {
        this.restClient = RestClient.create();
    }

    public DtoCanchaResponse consultarCancha(String token, Integer idCancha) {
        try {
            logger.info("Verificando Cancha ID: {} (Llevando token: {})", idCancha, token != null ? "SÍ" : "NO");
            
            return restClient.get()
                    .uri("http://localhost:8080/api/v1/canchas/" + idCancha)
                    .header("Authorization", token) 
                    .retrieve()
                    .body(DtoCanchaResponse.class);
                    
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Cancha con ID {} no encontrada (404) en ms-canchas.", idCancha);
            return null;
        } catch (Exception e) {
            logger.error("Error al consultar cancha {}: {}", idCancha, e.getMessage());
            return null;
        }
    }
}