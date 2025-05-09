package com.eviden.cine.service;

import com.eviden.cine.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfServiceTest {

    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService();
    }

    @Test
    void testGenerateReservationPdfReturnsNonEmptyByteArray() {
        // Crear datos mock
        Movie movie = new Movie();
        movie.setTitle("Inception");

        Room room = new Room();
        room.setNombreroom("Sala 1");

        Emision emision = new Emision();
        emision.setMovie(movie);
        emision.setRoom(room);
        emision.setFechaHoraInicio(LocalDateTime.of(2025, 5, 20, 18, 30));

        Asiento asiento = new Asiento();
        asiento.setFila("A");
        asiento.setColumna(5);

        ReservationDetails detail = new ReservationDetails();
        detail.setAsiento(asiento);

        Reservation reservation = new Reservation();
        reservation.setIdReserve(123L);
        reservation.setEmision(emision);
        reservation.setTotalPrice(9.99);
        reservation.setReserveDetails(Collections.singletonList(detail));
        reservation.setQrContent(null);

        // Act
        byte[] pdfBytes = pdfService.generateReservationPdf(reservation);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
