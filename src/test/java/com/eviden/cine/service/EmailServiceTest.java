package com.eviden.cine.service;

import com.eviden.cine.model.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);
    }

    @Test
    void enviarCorreoConfirmacion_enviaCorreoCorrectamente() throws MessagingException {

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        byte[] qrImage = "imagenQR".getBytes();

        User user = new User();
        user.setUsername("Carlos");

        Movie movie = new Movie();
        movie.setTitle("Inception");

        Room room = new Room();
        room.setNombreroom("Sala 5");

        Emision emision = new Emision();
        emision.setMovie(movie);
        emision.setFechaHoraInicio(LocalDateTime.of(2024, 5, 5, 20, 0));
        emision.setRoom(room);

        Asiento asiento = new Asiento();
        asiento.setFila("A");
        asiento.setColumna(3);

        ReservationDetails detail = new ReservationDetails();
        detail.setAsiento(asiento);

        Reservation reserva = new Reservation();
        reserva.setIdReserve(123L);
        reserva.setUser(user);
        reserva.setEmision(emision);
        reserva.setReserveDetails(List.of(detail));
        reserva.setTotalPrice(999.0);

        emailService.enviarCorreoConfirmacion("carlos@email.com", reserva, qrImage);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertNotNull(sentMessage);
    }

    @Test
    void enviarCorreoConfirmacion_lanzaExcepcionSiError()  {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doAnswer(invocation -> {
            throw new RuntimeException("Fallo en envío");
        }).when(mailSender).send(any(MimeMessage.class));

        Reservation reserva = new Reservation();
        reserva.setUser(new User());
        reserva.setEmision(new Emision());
        reserva.setReserveDetails(List.of());
        reserva.setTotalPrice(0.0);

        assertThrows(RuntimeException.class, () ->
            emailService.enviarCorreoConfirmacion("a@a.com", reserva, "qr".getBytes())
        );
    }
    @Test
    void sendPasswordResetEmail_enviaCorreoCorrectamente() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPasswordResetEmail("test@email.com", "dummy-token");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertNotNull(sentMessage);
    }

    @Test
    void sendPasswordResetEmail_lanzaExcepcionSiError() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new RuntimeException("Error al enviar")).when(mailSender).send(any(MimeMessage.class));

        assertThrows(RuntimeException.class, () -> {
            emailService.sendPasswordResetEmail("error@email.com", "token-error");
        });
    }

}
