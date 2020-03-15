package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.model.DigitalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DigitalUserRepository extends JpaRepository<DigitalUser, Long> {
    Optional<DigitalUser> findByCifEquals(String cif);
}
