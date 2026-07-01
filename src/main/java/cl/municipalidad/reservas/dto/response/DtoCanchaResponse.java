package cl.municipalidad.reservas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Estructura de respuesta con los datos detallados de la cancha consultada de forma interna")
public class DtoCanchaResponse {

    @Schema(description = "Identificador único de la cancha municipal", example = "1")
    private Integer idCancha;

    @Schema(description = "Nombre asignado al recinto deportivo", example = "Estadio Municipal N°1")
    private String nombre;

    @Schema(description = "Tipo de superficie o pasto de la cancha", example = "Sintético / Natural")
    private String tipoPasto;

    @Schema(description = "Indica si el recinto cuenta con focos de iluminación nocturna", example = "true")
    private Boolean tieneIluminacion;
}