package cl.municipalidad.reservas.dto.request;

import lombok.Data;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class DtoReservaRequest {
    
    @NotNull(message = "El ID de la cancha es obligatorio")
    private Integer idCancha;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long idUsuario;
    
    @NotNull(message = "La fecha de reserva es obligatoria")
    @FutureOrPresent(message = "La fecha de la reserva no puede estar en el pasado")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fechaReserva;
    
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;
    
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;
}