package com.eviden.cine.service;

import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Region;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.RegionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegionServiceTest {

    @Mock
    private RegionRepository regionRepo;

    @Mock
    private MovieRepository movieRepo;

    @InjectMocks
    private RegionService regionService;

    private Region region;
    private Movie movie;

    @BeforeEach
    public void setUp() {
        region = new Region();
        region.setId(1L);
        region.setName("RegionTest");

        movie = new Movie();
        movie.setId(1);
        movie.setTitle("MovieTest");
    }

    @Test
    public void testListRegions() {
        when(regionRepo.findAll()).thenReturn(Arrays.asList(region));
        List<Region> regions = regionService.listRegions();
        assertNotNull(regions);
        assertEquals(1, regions.size());
        assertEquals("RegionTest", regions.get(0).getName());
        verify(regionRepo).findAll();
    }

    @Test
    public void testMoviesInRegion() {
        when(movieRepo.findDistinctByRegionName("RegionTest")).thenReturn(Arrays.asList(movie));
        List<Movie> movies = regionService.moviesInRegion("RegionTest");
        assertNotNull(movies);
        assertEquals(1, movies.size());
        assertEquals("MovieTest", movies.get(0).getTitle());
        verify(movieRepo).findDistinctByRegionName("RegionTest");
    }

    @Test
    public void testObtenerPorIdFound() {
        when(regionRepo.findById(1L)).thenReturn(Optional.of(region));
        Region found = regionService.obtenerPorId(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
        verify(regionRepo).findById(1L);
    }

    @Test
    public void testObtenerPorIdNotFound() {
        when(regionRepo.findById(2L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            regionService.obtenerPorId(2L);
        });
        assertTrue(exception.getMessage().contains("Región no encontrada con ID: 2"));
        verify(regionRepo).findById(2L);
    }
}