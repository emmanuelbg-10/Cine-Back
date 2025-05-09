package com.eviden.cine.repository;

import com.eviden.cine.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer>, JpaSpecificationExecutor<Movie> {
    Optional<Movie> findMovieByTitle(String title); // buscar pelicula por titulo
    Optional<Movie> findMovieById(int id); // buscar pelicula por id
    List<Movie> findByGenreName(String genreName); // buscar pelicula por genero
    List<Movie> findByIsAvailableTrue(); // buscar peliculas disponibles
    List<Movie> findByIsComingSoonTrue(); // buscar peliculas proximamo estreno
    // buscar por keyword
    List<Movie> findByTitleContainingIgnoreCaseOrDirector_NameContainingIgnoreCaseOrCasting_NameContainingIgnoreCase(String title, String director, String actor);
    void deleteById(int id); // eliminar pelicula por id
    // buscar por region
    @Query("""
           SELECT DISTINCT e.movie
           FROM   Emision e
           WHERE  LOWER(e.room.region.name) = LOWER(:regionName)
           """)
    List<Movie> findDistinctByRegionName(@Param("regionName") String regionName);
}
