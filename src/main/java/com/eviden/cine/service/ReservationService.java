package com.eviden.cine.service;

import com.eviden.cine.dtos.AsientoTicketDTO;
import com.eviden.cine.dtos.ReservationRequestDTO;
import com.eviden.cine.dtos.ReservationResponseDTO;
import com.eviden.cine.model.*;
import com.eviden.cine.repository.*;
import com.google.zxing.WriterException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final AsientoRepository asientoRepo;
    private final TicketRepository ticketRepo;
    private final EmisionRepository emisionRepo;
    private final UserRepository userRepo;
    private final QrService qrService;
    private final PdfService pdfService;

    public ReservationService(
            ReservationRepository reservationRepo,
            AsientoRepository asientoRepo,
            TicketRepository ticketRepo,
            EmisionRepository emisionRepo,
            UserRepository userRepo,
            QrService qrService,
            PdfService pdfService) {
        this.reservationRepo = reservationRepo;
        this.asientoRepo = asientoRepo;
        this.ticketRepo = ticketRepo;
        this.emisionRepo = emisionRepo;
        this.userRepo = userRepo;
        this.qrService = qrService;
        this.pdfService = pdfService;
    }

    public Reservation findById(Long id) {
        return reservationRepo.findById(id).orElse(null);
    }

    public Reservation createConfirmedReservation(ReservationRequestDTO requestDTO) throws IOException, WriterException {
        User user = userRepo.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + requestDTO.getUserId()));
        Emision emision = emisionRepo.findById(requestDTO.getEmisionId())
                .orElseThrow(() -> new RuntimeException("Emisión no encontrada con ID: " + requestDTO.getEmisionId()));

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setEmision(emision);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setStatus("confirmed");
        reservation.setTotalPrice(requestDTO.getTotal() / 100);
        reservation.setReserveDetails(new ArrayList<>());

        for (AsientoTicketDTO item : requestDTO.getAsientos()) {
            Asiento asiento = asientoRepo.findById(item.getAsientoId())
                    .orElseThrow(() -> new RuntimeException("Asiento no encontrado con ID: " + item.getAsientoId()));
            Ticket ticket = ticketRepo.findById(item.getTicketId())
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado con ID: " + item.getTicketId()));

            asiento.setDisponible(false);
            asientoRepo.save(asiento);

            ReservationDetails detail = new ReservationDetails();
            detail.setAsiento(asiento);
            detail.setTicket(ticket);
            detail.setReservation(reservation);
            reservation.getReserveDetails().add(detail);
        }

        reservation = reservationRepo.save(reservation);

        String qrContent = """
            Reserva Nº: %d
            Emisión: %s
            Fecha: %s
            Hora: %s
            Sala: %s
            Asientos: %s
            Precio total: %.2f €
        """.formatted(
                reservation.getIdReserve(),
                reservation.getEmision().getMovie().getTitle(),
                reservation.getEmision().getFechaHoraInicio().toLocalDate(),
                reservation.getEmision().getFechaHoraInicio().toLocalTime(),
                reservation.getEmision().getRoom().getNombreroom(),
                reservation.getReserveDetails().stream()
                        .map(detail -> detail.getAsiento().getFila() + detail.getAsiento().getColumna())
                        .collect(Collectors.joining(", ")),
                reservation.getTotalPrice()
        );

        reservation.setQrContent(qrService.generateQr(qrContent));
        return reservationRepo.save(reservation);
    }

    public List<ReservationResponseDTO> getAllReservationsDTO() {
        return reservationRepo.findAll().stream()
                .map(this::buildReservationDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByEmailDTO(String email) {
        return reservationRepo.findByUserEmail(email).stream()
                .map(this::buildReservationDTO)
                .collect(Collectors.toList());
    }

    public ReservationResponseDTO getReservationById(Long id) {
        return reservationRepo.findById(id)
                .map(this::buildReservationDTO)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
    }

    public byte[] generatePdf(Reservation reservation) {
        return pdfService.generateReservationPdf(reservation);
    }

    public ReservationResponseDTO buildReservationDTO(Reservation res) {
        List<String> seats = res.getReserveDetails().stream()
                .map(d -> d.getAsiento().getFila() + String.valueOf(d.getAsiento().getColumna()))
                .collect(Collectors.toList());

        String qrBase64 = res.getQrContent() != null
                ? java.util.Base64.getEncoder().encodeToString(res.getQrContent())
                : null;

        return ReservationResponseDTO.builder()
                .idReserve(res.getIdReserve())
                .status(res.getStatus())
                .totalPrice(res.getTotalPrice())
                .reservationDate(res.getReservationDate().toString())
                .movieTitle(res.getEmision().getMovie().getTitle())
                .userEmail(res.getUser().getEmail())
                .userName(res.getUser().getUsername())
                .roomName(res.getEmision().getRoom().getNombreroom())
                .showTime(res.getEmision().getFechaHoraInicio().toString())
                .selectedSeats(seats)
                .qrBase64(qrBase64)
                .build();
    }
}
