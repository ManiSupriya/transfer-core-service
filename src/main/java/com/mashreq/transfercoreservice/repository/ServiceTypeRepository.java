package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
    Optional<ServiceType> findByCodeEquals(String code);
}