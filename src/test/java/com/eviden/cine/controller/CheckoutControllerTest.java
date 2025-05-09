package com.eviden.cine.controller;

import com.eviden.cine.component.SessionDataCache;
import com.eviden.cine.dtos.AsientoTicketDTO;
import com.eviden.cine.dtos.ReservationRequestDTO;
import com.eviden.cine.dtos.ReservationResponseDTO;
import com.eviden.cine.model.Reservation;
import com.eviden.cine.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.ApiException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutController.class)
@Import({CheckoutControllerTest.MockConfig.class, com.eviden.cine.config.SecurityConfig.class})
@WithMockUser
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SessionDataCache sessionDataCache;

    @MockBean
    private ReservationService reservationService;

    // Java
    @Test
    void getReservaBySessionId_found() throws Exception {
        Long reservaId = 77L;

        // Creación de objeto Reserva con datos mínimos obligatorios
        Reservation reserva = new Reservation();
        reserva.setIdReserve(reservaId);

        // Se crea un usuario dummy para evitar NPE en el DTO
        com.eviden.cine.model.User user = new com.eviden.cine.model.User();
        user.setEmail("test@example.com");
        user.setUsername("test");
        reserva.setUser(user);

        // Se crea una emisión dummy con movie y room para que el buildReservationDTO funcione
        com.eviden.cine.model.Emision emision = new com.eviden.cine.model.Emision();
        com.eviden.cine.model.Movie movie = new com.eviden.cine.model.Movie();
        movie.setTitle("Película Dummy");
        emision.setMovie(movie);

        com.eviden.cine.model.Room room = new com.eviden.cine.model.Room();
        room.setNombreroom("Sala Dummy");
        emision.setRoom(room);
        emision.setFechaHoraInicio(java.time.LocalDateTime.now());
        reserva.setEmision(emision);

        reserva.setReservationDate(java.time.LocalDateTime.now());
        reserva.setStatus("confirmed");
        reserva.setTotalPrice(10.0);
        reserva.setReserveDetails(java.util.Collections.emptyList());

        ReservationResponseDTO responseDTO = ReservationResponseDTO.builder()
                .idReserve(77L)
                .status("confirmed")
                .totalPrice(10.0)
                .reservationDate(reserva.getReservationDate().toString())
                .movieTitle(emision.getMovie().getTitle())
                .userEmail(user.getEmail())
                .userName(user.getUsername())
                .roomName(emision.getRoom().getNombreroom())
                .showTime(emision.getFechaHoraInicio().toString())
                .selectedSeats(List.of())
                .qrBase64(null)
                .build();

        when(sessionDataCache.getReservaId("abc123")).thenReturn(reservaId);
        when(reservationService.findById(reservaId)).thenReturn(reserva);
        when(reservationService.buildReservationDTO(reserva)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/checkout/reserva/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReserve").value(77));

    }

    @Test
    void getReservaBySessionId_notFound() throws Exception {
        when(sessionDataCache.getReservaId("xyz")).thenReturn(null);

        mockMvc.perform(get("/api/checkout/reserva/xyz"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReservaBySessionId_reservaNull() throws Exception {
        when(sessionDataCache.getReservaId("abc123")).thenReturn(42L);
        when(reservationService.findById(42L)).thenReturn(null);

        mockMvc.perform(get("/api/checkout/reserva/abc123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCheckoutSession_success() throws Exception {
        ReservationRequestDTO dto = new ReservationRequestDTO();
        dto.setUserId(1L);
        dto.setEmisionId(5L);
        dto.setTotal(30.0);
        dto.setAsientos(List.of(new AsientoTicketDTO(1L, 7L, "C4")));

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session session = mock(Session.class);
            when(session.getId()).thenReturn("sess_123");
            when(session.getUrl()).thenReturn("http://stripe.test/sess_123");

            mockedSession.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(session);

            mockMvc.perform(post("/api/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("sess_123"))
                    .andExpect(jsonPath("$.url").value("http://stripe.test/sess_123"));
        }
    }

    @Test
    void createCheckoutSession_stripeException() throws Exception {
        ReservationRequestDTO dto = new ReservationRequestDTO();
        dto.setUserId(1L);
        dto.setEmisionId(5L);
        dto.setTotal(30.0);
        dto.setAsientos(List.of(new AsientoTicketDTO(1L, 7L, "C4")));

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession
                    .when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new ApiException("Simulated error", "req_123", null, 500, null));

            mockMvc.perform(post("/api/checkout")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Error creando la sesión"));
        }
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public com.eviden.cine.security.JwtUtil jwtUtil() {
            return mock(com.eviden.cine.security.JwtUtil.class);
        }

        @Bean
        public com.eviden.cine.security.UserDetailsServiceImpl userDetailsService() {
            return mock(com.eviden.cine.security.UserDetailsServiceImpl.class);
        }
    }
}
