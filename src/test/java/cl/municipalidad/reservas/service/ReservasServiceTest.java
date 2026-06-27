package cl.municipalidad.reservas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import cl.municipalidad.reservas.client.CanchasClient;
import cl.municipalidad.reservas.dto.request.DtoReservaRequest;
import cl.municipalidad.reservas.dto.response.DtoCanchaResponse;
import cl.municipalidad.reservas.model.ReservasModel;
import cl.municipalidad.reservas.repository.ReservasRepository;

@ExtendWith(MockitoExtension.class)
class ReservasServiceTest {

    @Mock
    private ReservasRepository reservasRepository;

    @Mock
    private CanchasClient canchasClient;

    @InjectMocks
    private ReservasService reservasService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Simula Token
    private void mockUsuarioAutenticadoConToken(String nombre, String email) {
        Jwt jwt = Mockito.mock(Jwt.class);
        when(jwt.getClaimAsString("name")).thenReturn(nombre);
        when(jwt.getClaimAsString("email")).thenReturn(email);

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        SecurityContextHolder.setContext(securityContext);
    }

    // ── TEST 1: CREAR RESERVA  ──
    @Test
    void crearReserva_debeGuardarReservaCuandoDatosYTokenSonValidos() {
        // Arrange
        DtoReservaRequest request = new DtoReservaRequest();
        request.setIdCancha(10);
        request.setIdUsuario(5L);
        request.setFechaReserva(LocalDate.now().plusDays(1));
        request.setHoraInicio(LocalTime.of(14, 0));
        request.setHoraFin(LocalTime.of(15, 30));

        DtoCanchaResponse canchaMock = new DtoCanchaResponse();
        canchaMock.setIdCancha(10);
        canchaMock.setNombre("Estadio Municipal N°1");

        ReservasModel reservaGuardada = new ReservasModel();
        reservaGuardada.setIdReserva(100L);
        reservaGuardada.setEstadoReserva("PENDIENTE");
        reservaGuardada.setNombreCanchaForaneo("Estadio Municipal N°1");
        reservaGuardada.setCreadoPor("Tomas [tomas@municipalidad.cl]");

        
        mockUsuarioAutenticadoConToken("Tomas", "tomas@municipalidad.cl");

        
        when(canchasClient.consultarCancha(any(), eq(10))).thenReturn(canchaMock);
        when(reservasRepository.save(any(ReservasModel.class))).thenReturn(reservaGuardada);


        ReservasModel resultado = reservasService.crearReserva(request);


        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdReserva()).isEqualTo(100L);
        assertThat(resultado.getEstadoReserva()).isEqualTo("PENDIENTE");
        assertThat(resultado.getNombreCanchaForaneo()).isEqualTo("Estadio Municipal N°1");
        
        verify(canchasClient).consultarCancha(any(), eq(10));
        verify(reservasRepository).save(any(ReservasModel.class));
    }

    // ── TEST 2: FALLO POR HORAS INCONSISTENTES ──
    @Test
    void crearReserva_debeLanzarBadRequestCuandoHoraFinEsMenorOIgualALaDeInicio() {

        DtoReservaRequest request = new DtoReservaRequest();
        request.setIdCancha(10);
        request.setIdUsuario(5L);
        request.setFechaReserva(LocalDate.now().plusDays(1));
        request.setHoraInicio(LocalTime.of(18, 0));
        request.setHoraFin(LocalTime.of(17, 0));

        assertThatThrownBy(() -> reservasService.crearReserva(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("La hora de fin debe ser estrictamente posterior");

        verify(canchasClient, never()).consultarCancha(anyString(), any());
        verify(reservasRepository, never()).save(any());
    }

    // ── TEST 3: FALLO: LA CANCHA NO EXISTE EN EL OTRO MICROSERVICIO ──
    @Test
    void crearReserva_debeLanzarNotFoundCuandoMicroservicioCanchasRetornaNull() {
        // Arrange
        DtoReservaRequest request = new DtoReservaRequest();
        request.setIdCancha(999); // ID inexistente
        request.setIdUsuario(5L);
        request.setFechaReserva(LocalDate.now().plusDays(1));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFin(LocalTime.of(11, 0));

        when(canchasClient.consultarCancha(any(), eq(999))).thenReturn(null);

        assertThatThrownBy(() -> reservasService.crearReserva(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                .hasMessageContaining("La cancha especificada no existe");

        verify(reservasRepository, never()).save(any());
    }

    // ── TEST 4: ACTUALIZACIÓN Y CAMBIO DE ESTADO EXITOSO ──
    @Test
    void confirmarEstadoReserva_debeModificarEstadoCorrectamenteCuandoEsValido() {
        // Arrange
        Long reservaId = 1L;
        ReservasModel reservaExistente = new ReservasModel();
        reservaExistente.setIdReserva(reservaId);
        reservaExistente.setEstadoReserva("PENDIENTE");

        when(reservasRepository.findById(reservaId)).thenReturn(Optional.of(reservaExistente));
        when(reservasRepository.save(any(ReservasModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ReservasModel resultado = reservasService.confirmarEstadoReserva(reservaId, "CONFIRMADA");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstadoReserva()).isEqualTo("CONFIRMADA");
        verify(reservasRepository).findById(reservaId);
        verify(reservasRepository).save(reservaExistente);
    }

    // ── TEST 5: VALIDACIÓN DE LA CANCHA ESTRELLA CUANDO NO HAY RESERVAS ──
    @Test
    void obtenerCanchaEstrella_debeRetornarSinDatosCuandoListaEstaVacia() {
        // Arrange
        LocalDate inicio = LocalDate.now().minusMonths(1);
        LocalDate fin = LocalDate.now();

        when(reservasRepository.findCanchaEstrella(eq(inicio), eq(fin), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // Act
        String resultado = reservasService.obtenerCanchaEstrella(inicio, fin);

        // Assert
        assertThat(resultado).isEqualTo("Sin datos");
        verify(reservasRepository).findCanchaEstrella(eq(inicio), eq(fin), eq(PageRequest.of(0, 1)));
    }
}