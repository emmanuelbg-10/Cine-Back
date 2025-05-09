// Archivo: StripeWebhookControllerTest.java
package com.eviden.cine.controller;

import com.eviden.cine.component.SessionDataCache;
import com.eviden.cine.dtos.ReservationRequestDTO;
import com.eviden.cine.model.Reservation;
import com.eviden.cine.service.EmailService;
import com.eviden.cine.service.ReservationService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StripeWebhookControllerTest {

    private StripeWebhookController controller;
    private SessionDataCache sessionDataCache;
    private ReservationService reservationService;
    private EmailService emailService;

    private final String endpointSecret = "whsec_test_secret";

    @BeforeEach
    void setUp() {
        sessionDataCache = mock(SessionDataCache.class);
        reservationService = mock(ReservationService.class);
        emailService = mock(EmailService.class);

        controller = new StripeWebhookController(sessionDataCache, reservationService, emailService);
        controller.setEndpointSecret(endpointSecret);
    }

    @Test
    void testHandleStripeWebhook_CheckoutSessionCompleted_Success() throws Exception {
        Session session = mock(Session.class);
        Session.CustomerDetails customerDetails = mock(Session.CustomerDetails.class);
        when(customerDetails.getEmail()).thenReturn("cliente@example.com");
        when(session.getCustomerDetails()).thenReturn(customerDetails);
        when(session.getId()).thenReturn("sess_123");

        Event event = mock(Event.class);
        when(event.getType()).thenReturn("checkout.session.completed");
        Event.Data data = mock(Event.Data.class);
        when(event.getData()).thenReturn(data);
        when(data.getObject()).thenReturn(session);

        try (MockedStatic<Webhook> mockedWebhook = Mockito.mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), eq(endpointSecret)))
                    .thenReturn(event);

            ReservationRequestDTO requestDTO = mock(ReservationRequestDTO.class);
            when(sessionDataCache.getReservationData("sess_123")).thenReturn(requestDTO);

            Reservation reserva = new Reservation();
            reserva.setIdReserve(100L);
            reserva.setQrContent(new byte[]{1, 2, 3});
            when(reservationService.createConfirmedReservation(requestDTO)).thenReturn(reserva);

            ResponseEntity<String> response = controller.handleStripeWebhook("{}", "sig_header");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Webhook handled successfully", response.getBody());

            verify(sessionDataCache).saveReservaId("sess_123", 100L);
            verify(sessionDataCache).removeReservationData("sess_123");
            verify(emailService).enviarCorreoConfirmacion("cliente@example.com", reserva, reserva.getQrContent());
        }
    }

    @Test
    void testHandleStripeWebhook_EventNotSupported() throws Exception {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn("payment_intent.created");

        try (MockedStatic<Webhook> mockedWebhook = Mockito.mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), eq(endpointSecret)))
                    .thenReturn(event);

            ResponseEntity<String> response = controller.handleStripeWebhook("{}", "sig_header");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Event type not supported", response.getBody());
        }
    }

    @Test
    void testHandleStripeWebhook_ExceptionThrown() {
        try (MockedStatic<Webhook> mockedWebhook = Mockito.mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(anyString(), anyString(), eq(endpointSecret)))
                    .thenThrow(new RuntimeException("Simulated error"));

            ResponseEntity<String> response = controller.handleStripeWebhook("{}", "sig_header");

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().contains("Webhook error: Simulated error"));
        }
    }
}