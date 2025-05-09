package com.eviden.cine.controller;

import com.eviden.cine.dtos.AsientoTicketDTO;
import com.eviden.cine.dtos.ReservationRequestDTO;
import com.eviden.cine.dtos.ReservationResponseDTO;
import com.eviden.cine.model.Reservation;
import com.eviden.cine.service.ReservationService;
import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationControllerTest {

    private ReservationController controller;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = mock(ReservationService.class);
        controller = new ReservationController(reservationService);
    }

    @Test
    void testCreateReservation_Success() throws IOException, WriterException {
        // Crear DTO realista para la solicitud
        ReservationRequestDTO request = ReservationRequestDTO.builder()
                .userId(1L)
                .emisionId(5L)
                .asientos(List.of(
                        new AsientoTicketDTO(1L, 2L,"A1"),
                        new AsientoTicketDTO(13L, 2L,"F2")
                ))
                .total(2100D)
                .build();

        // Crear objeto de reserva simulado (puedes simplificar según tus entidades)
        Reservation mockReservation = new Reservation();
        mockReservation.setIdReserve(1001L);
        mockReservation.setStatus("confirmed");

        // Simular que el método devuelve la reserva
        when(reservationService.createConfirmedReservation(any(ReservationRequestDTO.class)))
                .thenReturn(mockReservation);

        // Ejecutar
        ResponseEntity<String> response = controller.createReservation(request);

        // Verificar
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Reserva creada exitosamente", response.getBody());
    }


    @Test
    void testCreateReservation_Failure() throws IOException, WriterException {
        ReservationRequestDTO request = mock(ReservationRequestDTO.class);

        doThrow(new RuntimeException("Error simulado")).when(reservationService).createConfirmedReservation(request);

        ResponseEntity<String> response = controller.createReservation(request);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Error al crear la reserva"));
    }

    @Test
    void testGetAllReservations() {
        List<ReservationResponseDTO> reservations = List.of(
                ReservationResponseDTO.builder()
                        .idReserve(1L)
                        .status("confirmed")
                        .totalPrice(21.0)
                        .reservationDate("2025-04-24T15:30:00")
                        .movieTitle("Inception")
                        .userEmail("usuario@cine.com")
                        .userName("Juan Pérez")
                        .roomName("Sala 1")
                        .showTime("2025-04-24T19:30:00")
                        .selectedSeats(List.of("A5", "A6"))
                        .qrBase64("data:image/png;base64,...")
                        .build()
        );

        when(reservationService.getAllReservationsDTO()).thenReturn(reservations);

        ResponseEntity<List<ReservationResponseDTO>> response = controller.getAllReservations();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(reservations, response.getBody());
    }


    @Test
    void testGetReservationsByEmail() {
        String email = "usuario@cine.com";
        List<ReservationResponseDTO> reservations = List.of(
                ReservationResponseDTO.builder()
                        .idReserve(2L)
                        .status("confirmed")
                        .totalPrice(15.0)
                        .reservationDate("2025-04-25T10:00:00")
                        .movieTitle("Avatar")
                        .userEmail(email)
                        .userName("Lucía Gómez")
                        .roomName("Sala 2")
                        .showTime("2025-04-25T20:00:00")
                        .selectedSeats(List.of("B1", "B2"))
                        .qrBase64("data:image/png;base64,QR...")
                        .build()
        );

        when(reservationService.getReservationsByEmailDTO(email)).thenReturn(reservations);

        ResponseEntity<List<ReservationResponseDTO>> response = controller.getReservationsByEmail(email);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(reservations, response.getBody());
    }


    @Test
    void testGetMyReservations() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("juan.perez@cine.com");

        List<ReservationResponseDTO> reservations = List.of(
                ReservationResponseDTO.builder()
                        .idReserve(1002L)
                        .status("confirmed")
                        .totalPrice(25.0)
                        .reservationDate("2025-04-26T13:45:00")
                        .movieTitle("Dune Part Two")
                        .userEmail("juan.perez@cine.com")
                        .userName("Juan Pérez")
                        .roomName("Sala IMAX")
                        .showTime("2025-04-26T18:00:00")
                        .selectedSeats(List.of("C1", "C2", "C3"))
                        .qrBase64("data:image/png;base64,MIIBIjANBgkqhki...")
                        .build()
        );

        when(reservationService.getReservationsByEmailDTO("juan.perez@cine.com")).thenReturn(reservations);

        ResponseEntity<List<ReservationResponseDTO>> response = controller.getMyReservations(auth);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(reservations, response.getBody());
        assertEquals("Dune Part Two", response.getBody().get(0).getMovieTitle());
        assertEquals("Sala IMAX", response.getBody().get(0).getRoomName());
        verify(reservationService).getReservationsByEmailDTO("juan.perez@cine.com");
    }


    @Test
    void testDownloadPdf_Success() {
        Long id = 1L;
        Reservation reservation = new Reservation();
        byte[] pdfBytes = new byte[]{1, 2, 3};

        when(reservationService.findById(id)).thenReturn(reservation);
        when(reservationService.generatePdf(reservation)).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = controller.downloadPdf(id);

        assertEquals(200, response.getStatusCodeValue());
        assertArrayEquals(pdfBytes, response.getBody());
        assertTrue(response.getHeaders().get("Content-Disposition").get(0).contains("attachment"));
    }

    @Test
    void testDownloadPdf_NotFound() {
        Long id = 99L;

        when(reservationService.findById(id)).thenReturn(null);

        ResponseEntity<byte[]> response = controller.downloadPdf(id);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetReservationById() {
        Long reservationId = 2001L;

        ReservationResponseDTO expectedDto = ReservationResponseDTO.builder()
                .idReserve(reservationId)
                .status("confirmed")
                .totalPrice(30.0)
                .reservationDate("2025-04-27T14:00:00")
                .movieTitle("Interstellar")
                .userEmail("maria.gomez@cine.com")
                .userName("María Gómez")
                .roomName("Sala 4D")
                .showTime("2025-04-27T20:00:00")
                .selectedSeats(List.of("D4", "D5"))
                .qrBase64("data:image/png;base64,FAKE_QR_CODE")
                .build();

        when(reservationService.getReservationById(reservationId)).thenReturn(expectedDto);

        ResponseEntity<ReservationResponseDTO> response = controller.getReservationById(reservationId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedDto, response.getBody());
        assertEquals("Interstellar", response.getBody().getMovieTitle());
        assertEquals("Sala 4D", response.getBody().getRoomName());
        verify(reservationService).getReservationById(reservationId);
    }

}
