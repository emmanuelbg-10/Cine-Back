package com.eviden.cine.repository;

import com.eviden.cine.model.Emision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmisionRepository extends JpaRepository<Emision, Long> {
    List<Emision> findByRoom_Region_Id(Long regionId);
    List<Emision> findByMovie_Id(Long movieId);
    List<Emision> findByMovie_TitleIgnoreCase(String title);
    List<Emision> findByRoom_Idroom(Long roomId);

}
