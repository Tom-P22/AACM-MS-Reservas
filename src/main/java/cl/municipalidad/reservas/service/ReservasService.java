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

    // Inyección por constructor
    public ReservasService(ReservasRepository reservasRepository, CanchasClient canchasClient) {
        this.reservasRepository = reservasRepository;
        this.canchasClient = canchasClient;
    }

    public ReservasModel crearReserva(DtoReservaRequest request) {

        HttpServletRequest requestOriginal = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String tokenBearer = requestOriginal.getHeader("Authorization");


        DtoCanchaResponse canchaRemota = canchasClient.consultarCancha(tokenBearer, request.getIdCancha());

        if (canchaRemota == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "No se puede crear la reserva: La cancha especificada no existe o el servicio no está disponible.");
        }

        //Extrae info operador
        String usuarioEjecutor = obtenerInfoUsuarioLog();

        ReservasModel nuevaReserva = new ReservasModel();
        nuevaReserva.setIdCancha(request.getIdCancha());
        nuevaReserva.setIdUsuario(request.getIdUsuario());
        nuevaReserva.setFechaReserva(request.getFechaReserva());
        nuevaReserva.setHoraInicio(request.getHoraInicio());
        nuevaReserva.setHoraFin(request.getHoraFin());
        nuevaReserva.setNombreCanchaForaneo(canchaRemota.getNombre());
        
        //Registra operador 
        nuevaReserva.setCreadoPor(usuarioEjecutor);

        ReservasModel reservaGuardada = reservasRepository.save(nuevaReserva);

        log.info("[RESERVA] Usuario {} AGENDÓ la cancha '{}' para el día {}", 
                usuarioEjecutor, canchaRemota.getNombre(), reservaGuardada.getFechaReserva());

        return reservaGuardada;
    }

    public ReservasModel confirmarEstadoReserva(Long idReserva, String nuevoEstado) {
        ReservasModel reserva = reservasRepository.findById(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada con el ID: " + idReserva));
        
        String estadoAnterior = reserva.getEstadoReserva();
        reserva.setEstadoReserva(nuevoEstado.toUpperCase());
        ReservasModel reservaActualizada = reservasRepository.save(reserva);

        log.info("[ESTADO]{} Usuario {} CAMBIÓ la reserva ID {} de '{}' a '{}'", 
                obtenerInfoUsuarioLog(), idReserva, estadoAnterior, reservaActualizada.getEstadoReserva());

        return reservaActualizada;
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
        return "Usuario del Sistema"; // Caída por defecto
    }

    public List<ReservasModel> obtenerReservasPorUsuario(Long idUsuario) {
    return reservasRepository.findByIdUsuario(idUsuario);
}
}