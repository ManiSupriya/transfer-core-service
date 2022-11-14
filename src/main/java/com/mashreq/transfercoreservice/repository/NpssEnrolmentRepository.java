package com.mashreq.transfercoreservice.repository;
import java.util.Optional;

import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NpssEnrolmentRepository extends JpaRepository<NpssEnrolmentRepoDTO, Long>  {
    @Query(value = "SELECT b from NpssEnrolmentRepoDTO b where cif_id=:cif")
    Optional<NpssEnrolmentRepoDTO> getEnrolmentStatus(String cif);

    @Query(value = "SELECT b from NpssEnrolmentRepoDTO b where cif_id=:cif")
    Optional<NpssEnrolmentRepoDTO> updateEnrolmentStatus(String cif);
}
