package com.eviden.cine.controller;

import com.eviden.cine.model.Asiento;
import com.eviden.cine.security.JwtUtil;
import com.eviden.cine.security.UserDetailsServiceImpl;
import com.eviden.cine.service.AsientoService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AsientoController.class)
@Import({AsientoControllerTest.MockConfig.class, com.eviden.cine.config.SecurityConfig.class})
class AsientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Asiento asiento;
    @SuppressWarnings("unused")
    private String asientoJson;

    @BeforeEach
    void setUp() {
        asiento = Asiento.builder()
                .idAsiento(1L)
                .fila("B")
                .columna(7)
                .tipoAsiento("VIP")
                .disponible(true)
                .build();

        asientoJson = """
                {
                    "idAsiento": 1,
                    "fila": "B",
                    "columna": 7,
                    "tipoAsiento": "VIP",
                    "disponible": true
                }
                """;
    }

    @WithMockUser
    @Test
    void testObtenerTodos() throws Exception {
        when(asientoService.obtenerTodosLosAsientos()).thenReturn(List.of(asiento));

        mockMvc.perform(get("/api/asientos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAsiento").value(1L))
                .andExpect(jsonPath("$[0].disponible").value(true));
    }

    @WithMockUser
    @Test
    void testObtenerPorIdEncontrado() throws Exception {
        when(asientoService.obtenerAsientoPorId(1L)).thenReturn(Optional.of(asiento));

        mockMvc.perform(get("/api/asientos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAsiento").value(1L))
                .andExpect(jsonPath("$.tipoAsiento").value("VIP"));
    }

    @WithMockUser
    @Test
    void testObtenerPorIdNoEncontrado() throws Exception {
        when(asientoService.obtenerAsientoPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/asientos/999"))
                .andExpect(status().isNotFound());
    }

    @WithMockUser
    @Test
    void testObtenerPorRoom() throws Exception {
        when(asientoService.obtenerAsientosPorroom(1L)).thenReturn(List.of(asiento));

        mockMvc.perform(get("/api/asientos/room/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAsiento").value(1L));
    }

    @WithMockUser
    @Test
    void testEliminarAsiento() throws Exception {
        doNothing().when(asientoService).eliminarAsiento(1L);

        mockMvc.perform(delete("/api/asientos/1"))
                .andExpect(status().isNoContent());
    }

    @WithMockUser
    @Test
    void testCambiarDisponibilidadMultiple() throws Exception {
        when(asientoService.cambiarDisponibilidad(List.of(1L))).thenReturn(List.of(asiento));

        mockMvc.perform(put("/api/asientos/disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAsiento").value(1L))
                .andExpect(jsonPath("$[0].disponible").value(true));
    }

    @WithMockUser
    @Test
    void testIsAsientoAvailable() throws Exception {
        when(asientoService.isAsientoAvailable(List.of(1L))).thenReturn(true);

        mockMvc.perform(post("/api/asientos/disponibilidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1]"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        public AsientoService asientoService() {
            return Mockito.mock(AsientoService.class);
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
