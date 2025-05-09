package com.eviden.cine.repository;

import com.eviden.cine.model.Review;
import com.eviden.cine.model.ReviewComent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewComentRepository extends JpaRepository<ReviewComent, Long> {


    List<ReviewComent> findByReview(Review review);
}
