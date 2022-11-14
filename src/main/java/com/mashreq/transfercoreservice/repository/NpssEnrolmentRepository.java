package com.mashreq.transfercoreservice.repository;
import java.util.Optional;

import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepo;
import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentResultsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NpssEnrolmentRepository extends JpaRepository<NpssEnrolmentRepo, Long>  {
    @Query(value = "SELECT b from NpssEnrolmentRepo b where cif_id=:cif")
    Optional<NpssEnrolmentResultsDto> getEnrolmentStatus(String cif);
}
