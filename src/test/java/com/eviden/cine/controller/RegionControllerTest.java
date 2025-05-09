package com.eviden.cine.controller;

import com.eviden.cine.dtos.EmisionFrontDTO;
import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Region;
import com.eviden.cine.service.EmisionService;
import com.eviden.cine.service.RegionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegionControllerTest {

    private RegionController regionController;
    private RegionService regionService;
    private EmisionService emisionService;

    @BeforeEach
    void setUp() {
        regionService = mock(RegionService.class);
        emisionService = mock(EmisionService.class);
        regionController = new RegionController(regionService, emisionService);
    }

    @Test
    void testGetAllRegions_ReturnsList() {
        List<Region> expectedRegions = List.of(new Region(1L, "Region 1",null), new Region(2L,"Region 2",null));
        when(regionService.listRegions()).thenReturn(expectedRegions);

        ResponseEntity<List<Region>> response = regionController.getAll();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(expectedRegions, response.getBody());
        verify(regionService).listRegions();
    }

    @Test
    void testMoviesByRegion_WithMovies_ReturnsOk() {
        String regionName = "Madrid";
        Movie movieMock = mock(Movie.class);
        List<Movie> movies = List.of(movieMock);
        when(regionService.moviesInRegion(regionName)).thenReturn(movies);

        ResponseEntity<List<Movie>> response = regionController.moviesByRegion(regionName);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(movies, response.getBody());
        verify(regionService).moviesInRegion(regionName);
    }

    @Test
    void testMoviesByRegion_NoMovies_ReturnsNoContent() {
        String regionName = "Madrid";
        when(regionService.moviesInRegion(regionName)).thenReturn(Collections.emptyList());

        ResponseEntity<List<Movie>> response = regionController.moviesByRegion(regionName);

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(regionService).moviesInRegion(regionName);
    }

    @Test
    void testEmisionesPorRegion_ReturnsList() {
        Long regionId = 1L;
        EmisionFrontDTO mockEmision = mock(EmisionFrontDTO.class);
        List<EmisionFrontDTO> emisiones = List.of(mockEmision);
        when(emisionService.emisionesPorRegion(regionId)).thenReturn(emisiones);

        ResponseEntity<List<EmisionFrontDTO>> response = regionController.emisionesPorRegion(regionId);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(emisiones, response.getBody());
        verify(emisionService).emisionesPorRegion(regionId);
    }

}
