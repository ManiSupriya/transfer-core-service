package com.mashreq.transfercoreservice.cardlesscash.repository;


import com.mashreq.transfercoreservice.model.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DigitalUserSegmentRepository extends JpaRepository<Segment, Long> {

    Optional<Segment> findByName(String name);
}
