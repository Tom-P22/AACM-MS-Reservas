package cl.municipalidad.reservas.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8080");
        gatewayServer.setDescription("API Gateway (Entorno Local)");

        return new OpenAPI()
            .info(new Info()
                .title("Servicio de Gestión de Reservas (ms-reservas)")
                .version("1.0.0")
                .description("Microservicio encargado del procesamiento, auditoría y control de reservas de canchas municipales. " +
                             "Se comunica de forma interna con ms-canchas para la verificación de disponibilidad de infraestructura.")
                .contact(new Contact()
                    .name("Departamento de Soporte TI - Municipalidad")
                    .email("soporte.ti@municipalidad.cl")))
            .servers(List.of(gatewayServer))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Introduce el Token JWT obtenido del microservicio de autenticación (ms-auth).")));
    }
}