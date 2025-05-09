package com.eviden.cine.controller;

import com.eviden.cine.component.SessionDataCache;
import com.eviden.cine.dtos.ReservationRequestDTO;
import com.eviden.cine.dtos.ReservationResponseDTO;
import com.eviden.cine.model.Reservation;
import com.eviden.cine.service.ReservationService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkout")
@Tag(name = "Checkout", description = "Operaciones relacionadas con la creación de sesiones de pago y reservas")
@RequiredArgsConstructor
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);
    private final SessionDataCache sessionDataCache;
    private final ReservationService reservationService;

    @Operation(
            summary = "Crear una sesión de pago en Stripe",
            description = "Genera una sesión de pago en Stripe para la reserva proporcionada en el body.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Sesión creada exitosamente",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error al crear la sesión",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody ReservationRequestDTO reservationRequestDTO) {
        try {
            String asientoList = reservationRequestDTO.getAsientos().stream()
                    .map(seat -> String.valueOf(seat.getAsientoSala()))
                    .collect(Collectors.joining(", "));

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setUnitAmount(reservationRequestDTO.getTotal().longValue())
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Entradas (Asientos: " + asientoList + ")")
                                    .build())
                            .build())
                    .setQuantity(1L)
                    .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:5173/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:5173/cancel")
                    .addLineItem(lineItem)
                    .build();

            Session session = Session.create(params);

            // Guardamos los datos en cache para ser procesados en el webhook
            sessionDataCache.saveReservationData(session.getId(), reservationRequestDTO);

            return ResponseEntity.ok(Map.of(
                    "id", session.getId(),
                    "url", session.getUrl()
            ));

        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error creando la sesión"));
        }
    }

    @Operation(
            summary = "Obtener reserva por ID de sesión",
            description = "Retorna los detalles de la reserva asociada a un ID de sesión de Stripe.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reserva encontrada",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Reservation.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Reserva no encontrada",
                            content = @Content
                    )
            }
    )
    @GetMapping("/reserva/{sessionId}")
    public ResponseEntity<ReservationResponseDTO> getReservaBySessionId(@PathVariable String sessionId) {
        logger.info("Buscando reserva para sessionId: {}", sessionId);

        Long reservaId = sessionDataCache.getReservaId(sessionId);
        if (reservaId == null) {
            return ResponseEntity.notFound().build();
        }

        Reservation reserva = reservationService.findById(reservaId);
        if (reserva == null) {
            return ResponseEntity.notFound().build();
        }

        ReservationResponseDTO dto = reservationService.buildReservationDTO(reserva);
        return ResponseEntity.ok(dto);
    }

}