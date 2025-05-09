package com.eviden.cine.repository;

import com.eviden.cine.model.Asiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {

    //encontramos los asientos por id room
    List<Asiento> findByroomIdroom(Long idroom);
}
