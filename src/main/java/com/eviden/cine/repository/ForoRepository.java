package com.eviden.cine.repository;

import com.eviden.cine.model.Foro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForoRepository extends JpaRepository<Foro, Long> {
}
