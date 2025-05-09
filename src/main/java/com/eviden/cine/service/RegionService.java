package com.eviden.cine.service;

import com.eviden.cine.model.Movie;
import com.eviden.cine.model.Region;
import com.eviden.cine.repository.MovieRepository;
import com.eviden.cine.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepo;
    private final MovieRepository movieRepo;

    public List<Region> listRegions() {
        return regionRepo.findAll();
    }

    public List<Movie> moviesInRegion(String regionName) {
        return movieRepo.findDistinctByRegionName(regionName);
    }

    public Region obtenerPorId(Long id) {
        return regionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Región no encontrada con ID: " + id));
    }

}
