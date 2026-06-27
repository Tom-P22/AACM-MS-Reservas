package cl.municipalidad.reservas.service;

import cl.municipalidad.reservas.client.CanchasClient;
import cl.municipalidad.reservas.dto.request.DtoReservaRequest;
import cl.municipalidad.reservas.dto.response.DtoCanchaResponse;
import cl.municipalidad.reservas.model.ReservasModel;
import cl.municipalidad.reservas.repository.ReservasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ReservasService {

    private final ReservasRepository reservasRepository;
    private final CanchasClient canchasClient;

    public ReservasService(ReservasRepository reservasRepository, CanchasClient canchasClient) {
        this.reservasRepository = reservasRepository;
        this.canchasClient = canchasClient;
    }

    public ReservasModel crearReserva(DtoReservaRequest request) {
        log.info("Iniciando proceso de creación de reserva para el usuario ID: {} en cancha ID: {}", 
                request.getIdUsuario(), request.getIdCancha());


        if (request.getHoraFin().isBefore(request.getHoraInicio()) || request.getHoraFin().equals(request.getHoraInicio())) {
            log.warn("Intento de reserva fallido: La hora de fin ({}) es anterior o igual a la de inicio ({})", 
                    request.getHoraFin(), request.getHoraInicio());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "La hora de fin debe ser estrictamente posterior a la hora de inicio.");
        }

        String token = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpRequest = attributes.getRequest();
                token = httpRequest.getHeader("Authorization");
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer el encabezado Authorization de la petición actual: {}", e.getMessage());
        }


        DtoCanchaResponse canchaDto = canchasClient.consultarCancha(token, request.getIdCancha());
        
        if (canchaDto == null) {
            log.error("Reserva rechazada: La cancha con ID {} no existe en el sistema central.", request.getIdCancha());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La cancha especificada no existe.");
        }

        ReservasModel nuevaReserva = new ReservasModel();
        nuevaReserva.setIdCancha(request.getIdCancha());
        nuevaReserva.setIdUsuario(request.getIdUsuario());
        nuevaReserva.setFechaReserva(request.getFechaReserva());
        nuevaReserva.setHoraInicio(request.getHoraInicio());
        nuevaReserva.setHoraFin(request.getHoraFin());
        nuevaReserva.setEstadoReserva("PENDIENTE");
        nuevaReserva.setNombreCanchaForaneo(canchaDto.getNombre());

       
        nuevaReserva.setCreadoPor(obtenerInfoUsuarioLog());

        ReservasModel guardada = reservasRepository.save(nuevaReserva);
        log.info("Reserva creada con éxito. ID Asignado: {} Estado: {}", guardada.getIdReserva(), guardada.getEstadoReserva());
        
        return guardada;
    }

    public ReservasModel confirmarEstadoReserva(Long id, String nuevoEstado) {
        log.info("Cambiando estado de reserva ID: {} a -> {}", id, nuevoEstado);
        
        ReservasModel reserva = reservasRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        String estadoNormalizado = nuevoEstado.toUpperCase();
        if (!estadoNormalizado.equals("PENDIENTE") && !estadoNormalizado.equals("CONFIRMADA") && !estadoNormalizado.equals("CANCELADA")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de reserva no válido");
        }

        reserva.setEstadoReserva(estadoNormalizado);
        return reservasRepository.save(reserva);
    }

    public Integer contarReservasPorRango(LocalDate inicio, LocalDate fin) {
        Integer conteo = reservasRepository.countByFechaReservaBetween(inicio, fin);
        return conteo != null ? conteo : 0;
    }

    public String obtenerCanchaEstrella(LocalDate inicio, LocalDate fin) {
        List<String> resultados = reservasRepository.findCanchaEstrella(inicio, fin, PageRequest.of(0, 1));
        
        if (resultados != null && !resultados.isEmpty() && resultados.get(0) != null) {
            return resultados.get(0);
        }
        return "Sin datos";
    }

    private String obtenerInfoUsuarioLog() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
                String nombre = jwt.getClaimAsString("name");
                String correo = jwt.getClaimAsString("email");
                
                nombre = (nombre != null) ? nombre : auth.getName();
                correo = (correo != null) ? correo : "sin-correo";
                
                return String.format("%s [%s]", nombre, correo);
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer la info del usuario", e);
        }
        return "Usuario del Sistema"; 
    }

    public List<ReservasModel> obtenerReservasPorUsuario(Long idUsuario) {
        return reservasRepository.findByIdUsuario(idUsuario);
    }
}