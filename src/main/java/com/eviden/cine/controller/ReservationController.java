package com.eviden.cine.controller;

import com.eviden.cine.dtos.ReservationRequestDTO;
import com.eviden.cine.dtos.ReservationResponseDTO;
import com.eviden.cine.model.Reservation;
import com.eviden.cine.service.ReservationService;
import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Operaciones relacionadas con las reservas")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Operation(
            summary = "Crear reserva confirmada",
            description = "Crea una reserva confirmada con los datos de usuario, emisión, asientos y precio. Se genera un código QR al finalizar.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos necesarios para crear la reserva",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReservationRequestDTO.class),
                            examples = @ExampleObject(value = """
                {
                  "userId": 1,
                  "emisionId": 5,
                  "asientos": [
                    { "asientoId": 12, "ticketId": 2 },
                    { "asientoId": 13, "ticketId": 2 }
                  ],
                  "total": 2100
                }
                """)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva creada exitosamente", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Error al crear la reserva", content = @Content)
    })
    @PostMapping("/confirm")
    public ResponseEntity<String> createReservation(@RequestBody ReservationRequestDTO requestDTO) {
        try {
            reservationService.createConfirmedReservation(requestDTO);
            return ResponseEntity.ok("Reserva creada exitosamente");
        } catch (RuntimeException | IOException | WriterException e) {
            return ResponseEntity.badRequest().body("Error al crear la reserva: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Obtener todas las reservas",
            description = "Devuelve una lista de todas las reservas realizadas con información completa de cada una."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de reservas",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponseDTO.class))
    )
    @GetMapping("/all")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservationsDTO());
    }

    @Operation(
            summary = "Obtener reservas por email de usuario",
            description = "Retorna todas las reservas asociadas al correo electrónico proporcionado.",
            parameters = @Parameter(name = "email", description = "Correo del usuario a consultar", required = true)
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de reservas por email",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponseDTO.class))
    )
    @GetMapping("/by-email")
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByEmail(
            @RequestParam("email") String email) {
        return ResponseEntity.ok(reservationService.getReservationsByEmailDTO(email));
    }

    @Operation(
            summary = "Historial del usuario autenticado",
            description = "Devuelve todas las reservas del usuario actualmente autenticado utilizando JWT."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de reservas del usuario autenticado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponseDTO.class))
    )
    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationResponseDTO>> getMyReservations(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(reservationService.getReservationsByEmailDTO(email));
    }

    @Operation(
            summary = "Descargar entrada en PDF",
            description = "Genera y permite la descarga de un archivo PDF con los detalles de la reserva, incluyendo QR."
    )
    @ApiResponse(
            responseCode = "200",
            description = "PDF generado correctamente",
            content = @Content(mediaType = "application/pdf")
    )
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Reservation reservation = reservationService.findById(id);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdf = reservationService.generatePdf(reservation);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=entrada_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(
            summary = "Obtener reserva por ID",
            description = "Devuelve los detalles de una reserva específica a partir de su identificador único."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Detalles de la reserva",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponseDTO.class))
    )
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }
}
