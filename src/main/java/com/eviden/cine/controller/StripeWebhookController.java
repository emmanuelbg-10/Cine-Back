package com.eviden.cine.controller;

import com.eviden.cine.component.SessionDataCache;
import com.eviden.cine.dtos.ReservationRequestDTO;
import com.eviden.cine.model.Reservation;
import com.eviden.cine.service.EmailService;
import com.eviden.cine.service.ReservationService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/webhook")
@Tag(name = "StripeWebhook", description = "Gestiona los eventos de Stripe")
public class StripeWebhookController {

    private final SessionDataCache sessionDataCache;
    private final ReservationService reservationService;
    private final EmailService emailService;

    // Añadir este método en StripeWebhookController
    @Setter
    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    public StripeWebhookController(SessionDataCache sessionDataCache,ReservationService reservationService, EmailService emailService) {
        this.sessionDataCache = sessionDataCache;
        this.reservationService=reservationService;
        this.emailService=emailService;
    }

    @Operation(
            summary = "Procesar webhook de Stripe",
            description = "Maneja el evento webhook proveniente de Stripe. Cuando se completa una sesión de checkout, se procesa la reserva, se genera el QR y se envía el correo de confirmación."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Webhook procesado exitosamente",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Error al procesar el webhook o evento no soportado",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
    )
    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getData().getObject();
                String customerEmail = session.getCustomerDetails().getEmail();
                ReservationRequestDTO reservationRequestDTO = sessionDataCache.getReservationData(session.getId());

                if (reservationRequestDTO != null) {
                    Reservation reserva = reservationService.createConfirmedReservation(reservationRequestDTO);
                    sessionDataCache.saveReservaId(session.getId(), reserva.getIdReserve());
                    sessionDataCache.removeReservationData(session.getId());
                    //Crear QR
                    byte[] qrImage = reserva.getQrContent();
                    //Enviar el correo
                    emailService.enviarCorreoConfirmacion(customerEmail, reserva, qrImage);
                }

                return ResponseEntity.ok("Webhook handled successfully");
            } else {
                return ResponseEntity.status(400).body("Event type not supported");
            }

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook error: " + e.getMessage());
        }
    }
}
