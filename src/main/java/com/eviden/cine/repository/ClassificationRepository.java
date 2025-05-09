package com.eviden.cine.repository;

import com.eviden.cine.model.Classification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassificationRepository extends JpaRepository<Classification, Integer> {
    Optional<Classification> findByName(String name);
    Optional<Classification> findById(int id);
    Classification save(Classification classification);
    void deleteById(int id);
}
