package cl.municipalidad.reservas.controller;

import cl.municipalidad.reservas.dto.request.DtoReservaRequest;
import cl.municipalidad.reservas.model.ReservasModel;
import cl.municipalidad.reservas.service.ReservasService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservasController {

    private final ReservasService reservasService;

    public ReservasController(ReservasService reservasService) {
        this.reservasService = reservasService;
    }


    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ReservasModel>> obtenerReservasPorUsuario(@PathVariable("idUsuario") Long idUsuario) {
    List<ReservasModel> reservas = reservasService.obtenerReservasPorUsuario(idUsuario);
    return ResponseEntity.ok(reservas);
}

    @PostMapping
    public ResponseEntity<ReservasModel> generarNuevaReserva(@Valid @RequestBody DtoReservaRequest request) {
        ReservasModel reservaGuardada = reservasService.crearReserva(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaGuardada);
    }


    @PutMapping("/{id}/estado")
    public ResponseEntity<ReservasModel> actualizarEstado(
            @PathVariable("id") Long id,
            @RequestParam("nuevoEstado") String nuevoEstado) {
        
        ReservasModel reservaActualizada = reservasService.confirmarEstadoReserva(id, nuevoEstado);
        return ResponseEntity.ok(reservaActualizada);
    }

 
    @GetMapping("/analitica/conteo")
    public ResponseEntity<Integer> obtenerTotalReservas(
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("fechaFin") LocalDate fechaFin) {
        
        Integer total = reservasService.contarReservasPorRango(fechaInicio, fechaFin);
        return ResponseEntity.ok(total);
    }

    //Buscar cancha mas solicitada
    @GetMapping("/analitica/cancha-estrella")
    public ResponseEntity<String> obtenerCanchaEstrella(
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("fechaFin") LocalDate fechaFin) {
        
        String cancha = reservasService.obtenerCanchaEstrella(fechaInicio, fechaFin);
        return ResponseEntity.ok(cancha);
    }
}