package com.eviden.cine.repository;

import com.eviden.cine.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByNameIgnoreCase(String name);
}
