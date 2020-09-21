package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.model.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegmentMsRepository extends JpaRepository<Segment, Long> {
}
