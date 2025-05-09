package com.eviden.cine.repository;
import com.eviden.cine.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> findByName(String name);
    Optional<Genre> findById(int id);
    Genre save(Genre genre);
    void deleteById(int id);
}
