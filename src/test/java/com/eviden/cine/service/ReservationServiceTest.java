package com.eviden.cine.service;

import com.eviden.cine.dtos.AsientoTicketDTO;
import com.eviden.cine.dtos.ReservationRequestDTO;
import com.eviden.cine.model.*;
import com.eviden.cine.repository.*;
import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private EmisionRepository emisionRepo;
    @Mock
    private AsientoRepository asientoRepo;
    @Mock
    private PdfService pdfService;
    @Mock
    private TicketRepository ticketRepo;
    @Mock
    private QrService qrService;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Java
    private Reservation createMockReservation() {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("testuser@example.com");
        user.setUsername("TestUser");

        Movie movie = new Movie();
        movie.setTitle("Mock Movie Title");

        Room room = new Room();
        room.setNombreroom("Sala 1");

        Emision emision = new Emision();
        emision.setMovie(movie);
        emision.setRoom(room);
        emision.setFechaHoraInicio(LocalDateTime.now());

        Asiento asiento = new Asiento();
        asiento.setFila("A");
        asiento.setColumna(5);

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setName("Entrada General");

        ReservationDetails detail = new ReservationDetails();
        detail.setAsiento(asiento);
        detail.setTicket(ticket);

        Reservation reservation = new Reservation();
        reservation.setIdReserve(1L);
        reservation.setUser(user);
        reservation.setEmision(emision);
        reservation.setTotalPrice(12.5);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setStatus("confirmed");
        reservation.setReserveDetails(List.of(detail));
        reservation.setQrContent(new byte[]{1, 2, 3});

        return reservation;
    }

    // Java
    @Test
    void testCreateConfirmedReservation_success() throws IOException, WriterException {
        ReservationRequestDTO dto = new ReservationRequestDTO();
        dto.setUserId(1L);
        dto.setEmisionId(1L);
        dto.setAsientos(List.of(
                new AsientoTicketDTO(1L, 1L, "A5"),
                new AsientoTicketDTO(2L, 1L, "A6")
        ));
        dto.setTotal(25.0);

        User user = new User();
        Emision emision = new Emision();
        emision.setMovie(new Movie());
        emision.setRoom(new Room());
        emision.setFechaHoraInicio(LocalDateTime.now());
        Asiento asiento1 = new Asiento();
        asiento1.setIdAsiento(1L);
        Asiento asiento2 = new Asiento();
        asiento2.setIdAsiento(2L);

        // Configurar los mocks para las búsquedas
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(emisionRepo.findById(1L)).thenReturn(Optional.of(emision));
        when(asientoRepo.findById(1L)).thenReturn(Optional.of(asiento1));
        when(asientoRepo.findById(2L)).thenReturn(Optional.of(asiento2));
        when(qrService.generateQr(anyString())).thenReturn(new byte[]{1, 2, 3});
        // Agregar mocks para ticketRepo
        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setName("Entrada A");
        Ticket ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setName("Entrada B");
        when(ticketRepo.findById(1L)).thenReturn(Optional.of(ticket1));
        when(ticketRepo.findById(2L)).thenReturn(Optional.of(ticket2));

        when(reservationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = reservationService.createConfirmedReservation(dto);

        assertNotNull(result);
        assertEquals(2, result.getReserveDetails().size());
    }
    @Test
    void testGetReservationsByEmailDTO() {
        Reservation mockReservation = createMockReservation();
        when(reservationRepo.findByUserEmail("testuser@example.com")).thenReturn(List.of(mockReservation));

        var dtos = reservationService.getReservationsByEmailDTO("testuser@example.com");

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
    }

    @Test
    void testGetAllReservationsDTO() {
        Reservation mockReservation = createMockReservation();
        when(reservationRepo.findAll()).thenReturn(List.of(mockReservation));

        var dtos = reservationService.getAllReservationsDTO();

        assertNotNull(dtos);
        assertEquals(1, dtos.size());
    }

    @Test
    void testGetReservationById_found() {
        Reservation mockReservation = createMockReservation();
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(mockReservation));

        var dto = reservationService.getReservationById(1L);

        assertNotNull(dto);
        assertEquals("Mock Movie Title", dto.getMovieTitle());
    }

    // Java
    @Test
    void testGetReservationById_notFound() {
        when(reservationRepo.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            reservationService.getReservationById(1L);
        });

        assertEquals("Reserva no encontrada con ID: 1", thrown.getMessage());
    }

    @Test
    void testGeneratePdf() {
        Reservation mockReservation = createMockReservation();
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(mockReservation));
        when(pdfService.generateReservationPdf(any(Reservation.class))).thenReturn(new byte[]{1, 2, 3});

        byte[] pdf = reservationService.generatePdf(mockReservation);

        assertNotNull(pdf);
        assertEquals(3, pdf.length);
    }
}
