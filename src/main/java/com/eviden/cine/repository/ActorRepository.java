package com.eviden.cine.repository;

import com.eviden.cine.model.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Integer> {
    List<Actor> findByNameContainingIgnoreCase(String name);
    Optional<Actor> findByNameIgnoreCase(String name);
}
