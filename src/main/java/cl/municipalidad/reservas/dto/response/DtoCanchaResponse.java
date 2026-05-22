package cl.municipalidad.reservas.dto.response;

import lombok.Data;

@Data
public class DtoCanchaResponse {
    private Integer idCancha;
    private String nombre;
    private String tipoPasto;
    private Boolean tieneIluminacion;
}