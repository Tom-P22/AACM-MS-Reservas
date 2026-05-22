package cl.municipalidad.reservas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservasModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    @Column(nullable = false)
    private Integer idCancha;

    @Column(nullable = false)
    private Long idUsuario;

    @Column(nullable = false)
    private LocalDate fechaReserva; 

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private String estadoReserva; // PENDIENTE, CONFIRMADA, CANCELADA

    // No le ponemos nullable = false por si el ms-canchas falla de forma temporal
    private String nombreCanchaForaneo; 

    // Auditoria
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor; // Guardará el correo de quien ejecutó la acción

    // Ciclo de vida de reserva
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now(); 
        if (this.estadoReserva == null) {
            this.estadoReserva = "PENDIENTE"; 
    }
}
}