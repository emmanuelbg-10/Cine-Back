package com.eviden.cine.repository;

import com.eviden.cine.model.Director;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Integer> {
    List<Director> findByNameContainingIgnoreCase(String name);
    Optional<Director> findByName(String name);
}
