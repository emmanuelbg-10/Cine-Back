// src/test/java/com/eviden/cine/controller/EmisionControllerTest.java
package com.eviden.cine.controller;

import com.eviden.cine.dtos.EmisionDTO;
import com.eviden.cine.dtos.EmisionFrontDTO;
import com.eviden.cine.dtos.EmisionResponseDTO;
import com.eviden.cine.model.Emision;
import com.eviden.cine.model.Emision.EstadoEmision;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Room;
import com.eviden.cine.security.JwtUtil;
import com.eviden.cine.security.UserDetailsServiceImpl;
import com.eviden.cine.service.EmisionService;
import com.eviden.cine.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmisionController.class)
@Import({EmisionControllerTest.MockConfig.class, com.eviden.cine.config.SecurityConfig.class})
class EmisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmisionService emisionService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    private Emision emision;
    private EmisionDTO dto;

    @BeforeEach
    void setUp() {
        dto = EmisionDTO.builder()
                .idPelicula(1)
                .idRoom(1L)
                .fechaHoraInicio(LocalDateTime.of(2025, 6, 12, 20, 30))
                .idioma("Español subtitulado")
                .estado(EstadoEmision.ACTIVO)
                .build();
        Movie movie = new Movie();
        movie.setId(1);
        movie.setTitle("Película de prueba");

        Room room = new Room();
        room.setIdroom(1L);
        room.setNombreroom("Sala de Prueba");
        room.setCapacidad(100);

        emision = Emision.builder()
                .idEmision(1L)
                .fechaHoraInicio(dto.getFechaHoraInicio())
                .idioma(dto.getIdioma())
                .estado(dto.getEstado())
                .movie(movie)
                .room(room)
                .build();
    }

    @WithMockUser
    @Test
    void testListarTodas() throws Exception {
        when(emisionService.obtenerTodas()).thenReturn(List.of(emision));

        mockMvc.perform(get("/api/emisiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idEmision").value(1L));
    }

    @WithMockUser
    @Test
    void testObtenerPorIdEncontrado() throws Exception {
        when(emisionService.obtenerPorId(1L)).thenReturn(Optional.of(emision));
        // Se espera la propiedad "id" según el mapeo en EmisionSimple2DTO
        mockMvc.perform(get("/api/emisiones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @WithMockUser
    @Test
    void testObtenerPorIdNoEncontrado() throws Exception {
        when(emisionService.obtenerPorId(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/emisiones/2"))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testEliminarEmision() throws Exception {
        doNothing().when(emisionService).eliminar(1L);

        mockMvc.perform(delete("/api/emisiones/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testCrearEmision() throws Exception {
        EmisionDTO inputDto = EmisionDTO.builder()
                .idPelicula(1)
                .idRoom(1L)
                .fechaHoraInicio(LocalDateTime.of(2025, 6, 12, 20, 30))
                .idioma("Español subtitulado")
                .estado(EstadoEmision.ACTIVO)
                .build();
        Emision nuevaEmision = Emision.builder()
                .idEmision(2L)
                .fechaHoraInicio(inputDto.getFechaHoraInicio())
                .idioma(inputDto.getIdioma())
                .estado(inputDto.getEstado())
                .movie(emision.getMovie())
                .room(emision.getRoom())
                .build();
        // Se usa el builder para crear EmisionResponseDTO
        EmisionResponseDTO responseDto = EmisionResponseDTO.builder()
                .idEmision(nuevaEmision.getIdEmision())
                .movieTitle(nuevaEmision.getMovie().getTitle())
                .roomId(nuevaEmision.getRoom().getIdroom())
                .roomName(nuevaEmision.getRoom().getNombreroom())
                .roomCapacity(nuevaEmision.getRoom().getCapacidad())
                .fechaHoraInicio(nuevaEmision.getFechaHoraInicio())
                .idioma(nuevaEmision.getIdioma())
                .estado(nuevaEmision.getEstado())
                .build();

        when(emisionService.guardarDesdeDTO(any(EmisionDTO.class))).thenReturn(nuevaEmision);
        when(emisionService.toEmisionResponseDTO(nuevaEmision)).thenReturn(responseDto);

        mockMvc.perform(post("/api/emisiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEmision").value(nuevaEmision.getIdEmision()));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testActualizarEmision() throws Exception {

        EmisionDTO inputDto = EmisionDTO.builder()
                .idPelicula(1)
                .idRoom(1L)
                .fechaHoraInicio(LocalDateTime.of(2025, 6, 12, 22, 0))
                .idioma("Español doblado")
                .estado(EstadoEmision.ACTIVO)
                .build();
        Emision updatedEmision = Emision.builder()
                .idEmision(1L)
                .fechaHoraInicio(inputDto.getFechaHoraInicio())
                .idioma(inputDto.getIdioma())
                .estado(inputDto.getEstado())
                .movie(emision.getMovie())
                .room(emision.getRoom())
                .build();
        EmisionResponseDTO responseDto = EmisionResponseDTO.builder()
                .idEmision(updatedEmision.getIdEmision())
                .movieTitle(updatedEmision.getMovie().getTitle())
                .roomId(updatedEmision.getRoom().getIdroom())
                .roomName(updatedEmision.getRoom().getNombreroom())
                .roomCapacity(updatedEmision.getRoom().getCapacidad())
                .fechaHoraInicio(updatedEmision.getFechaHoraInicio())
                .idioma(updatedEmision.getIdioma())
                .estado(updatedEmision.getEstado())
                .build();

        when(emisionService.actualizarDesdeDTO(1L, inputDto)).thenReturn(updatedEmision);
        when(emisionService.toEmisionResponseDTO(updatedEmision)).thenReturn(responseDto);

        mockMvc.perform(put("/api/emisiones/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEmision").value(updatedEmision.getIdEmision()));
    }

    // src/test/java/com/eviden/cine/controller/EmisionControllerTest.java
    @WithMockUser
    @Test
    void testGetEmisionesByMovieId() throws Exception {
        // Se utiliza la emisión para que el mapeo en el controlador funcione correctamente.
        when(emisionService.getEmisionesByMovieId(1L)).thenReturn(List.of(emision));

        mockMvc.perform(get("/api/emisiones/pelicula/1"))
                .andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    void testGetEmisionesPorSala() throws Exception {
        EmisionFrontDTO frontDto = EmisionFrontDTO.of(emision);
        when(emisionService.obtenerEmisionesPorSala(1L)).thenReturn(List.of(frontDto));

        mockMvc.perform(get("/api/emisiones/sala/1"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testGenerarEmisionesAutomaticamente_success() throws Exception {
        doNothing().when(emisionService).generarEmisionesAutomaticas();

        mockMvc.perform(post("/api/emisiones/generar-automatico")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Emisiones generadas correctamente."));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void testGenerarEmisionesAutomaticamente_fail() throws Exception {
        doThrow(new RuntimeException("Error")).when(emisionService).generarEmisionesAutomaticas();

        mockMvc.perform(post("/api/emisiones/generar-automatico")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(Matchers.containsString("Error al generar emisiones")));
    }



    @TestConfiguration
    static class MockConfig {
        @Bean
        public EmisionService emisionService() {
            return Mockito.mock(EmisionService.class);
        }
        @Bean
        public MovieService movieService() {
            return Mockito.mock(MovieService.class);
        }
        @Bean
        public JwtUtil jwtUtil() {
            return Mockito.mock(JwtUtil.class);
        }
        @Bean
        public UserDetailsServiceImpl userDetailsService() {
            return Mockito.mock(UserDetailsServiceImpl.class);
        }
    }
}