package cl.municipalidad.reservas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
@Schema(description = "Modelo de petición para la creación de una reserva")
public class DtoReservaRequest {
    
    @NotNull(message = "El ID de la cancha es obligatorio")
    @Schema(description = "ID único de la cancha mapeado desde ms-canchas", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer idCancha;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    @Schema(description = "ID del ciudadano que realiza la reserva", example = "105", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idUsuario;
    
    @NotNull(message = "La fecha de reserva es obligatoria")
    @FutureOrPresent(message = "La fecha de la reserva no puede estar en el pasado")
    @JsonFormat(pattern = "dd-MM-yyyy")
    @Schema(description = "Fecha planificada de la reserva en formato dd-MM-yyyy. Debe ser hoy o a futuro.", example = "15-08-2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaReserva;
    
    @NotNull(message = "La hora de inicio es obligatoria")
    @Schema(description = "Hora de inicio del bloque deportivo (HH:mm:ss)", example = "14:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaInicio;
    
    @NotNull(message = "La hora de fin es obligatoria")
    @Schema(description = "Hora de finalización del bloque deportivo (HH:mm:ss)", example = "15:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaFin;
}