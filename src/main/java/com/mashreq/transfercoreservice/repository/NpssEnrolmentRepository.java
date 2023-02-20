package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NpssEnrolmentRepository extends JpaRepository<NpssEnrolmentRepoDTO, Long>  {
    @Query(value = "SELECT b from NpssEnrolmentRepoDTO b where cif_id=:cif")
    Optional<NpssEnrolmentRepoDTO> getEnrolmentStatus(String cif);
}
