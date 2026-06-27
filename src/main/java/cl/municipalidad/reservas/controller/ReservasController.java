package cl.municipalidad.reservas.controller;

import cl.municipalidad.reservas.dto.request.DtoReservaRequest;
import cl.municipalidad.reservas.model.ReservasModel;
import cl.municipalidad.reservas.service.ReservasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Módulo de Reservas", description = "Endpoints para la creación, consulta y analítica operacional de reservas de canchas.")
public class ReservasController {

    private final ReservasService reservasService;

    public ReservasController(ReservasService reservasService) {
        this.reservasService = reservasService;
    }


    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener reservas por ID de usuario", description = "Recupera un listado histórico de todas las reservas asociadas a un ciudadano específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado recuperado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservasModel.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado - Token inválido o no provisto", content = @Content),
        @ApiResponse(responseCode = "403", description = "Prohibido - No tienes permisos para ver este recurso", content = @Content)
    })
    public ResponseEntity<List<ReservasModel>> obtenerReservasPorUsuario(@PathVariable("idUsuario") Long idUsuario) {
    List<ReservasModel> reservas = reservasService.obtenerReservasPorUsuario(idUsuario);
    return ResponseEntity.ok(reservas);
}

    @PostMapping
    @Operation(summary = "Generar una nueva reserva", description = "Registra una reserva validando internamente la existencia de la cancha mediante comunicación asíncrona/síncrona con ms-canchas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reserva creada con éxito",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservasModel.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o error en validaciones de negocio (fechas pasadas, etc.)", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<ReservasModel> generarNuevaReserva(@Valid @RequestBody DtoReservaRequest request) {
        ReservasModel reservaGuardada = reservasService.crearReserva(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaGuardada);
    }


    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de una reserva", description = "Permite a los administradores modificar el estado operacional de una reserva (CONFIRMADA, CANCELADA, PENDIENTE).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado de reserva actualizado correctamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservasModel.class))),
        @ApiResponse(responseCode = "400", description = "Transición de estado no válida", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content),
        @ApiResponse(responseCode = "404", description = "ID de reserva no encontrado", content = @Content)
    })
    public ResponseEntity<ReservasModel> actualizarEstado(
            @Parameter(description = "ID de la reserva a modificar", example = "1", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "Nuevo estado aplicable", example = "CONFIRMADA", required = true)
            @RequestParam("nuevoEstado") String nuevoEstado) {

        ReservasModel reservaActualizada = reservasService.confirmarEstadoReserva(id, nuevoEstado);
        return ResponseEntity.ok(reservaActualizada);
    }

 
    @GetMapping("/analitica/conteo")
    @Operation(summary = "Contar total de reservas por rango de fechas", description = "Endpoint administrativo para obtener métricas cuantitativas globales dentro de un rango determinado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Métrica calculada con éxito",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<Integer> obtenerTotalReservas(
            @Parameter(description = "Fecha de inicio del reporte (AAAA-MM-DD)", example = "2026-01-01", required = true)
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin del reporte (AAAA-MM-DD)", example = "2026-12-31", required = true)
            @RequestParam("fechaFin") LocalDate fechaFin) {
        
        Integer total = reservasService.contarReservasPorRango(fechaInicio, fechaFin);
        return ResponseEntity.ok(total);
    }

    //Buscar cancha mas solicitada
    @GetMapping("/analitica/cancha-estrella")
    @Operation(summary = "Obtener nombre de la cancha más solicitada", description = "Analiza las reservas del rango indicado y retorna el nombre de la cancha con mayor demanda.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Nombre de cancha resuelto",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, example = "Estadio Municipal N°1"))),
        @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<String> obtenerCanchaEstrella(
            @Parameter(description = "Fecha inicial de análisis", example = "2026-06-01", required = true)
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @Parameter(description = "Fecha final de análisis", example = "2026-06-30", required = true)
            @RequestParam("fechaFin") LocalDate fechaFin) {

        String cancha = reservasService.obtenerCanchaEstrella(fechaInicio, fechaFin);
        return ResponseEntity.ok(cancha);
    }
}