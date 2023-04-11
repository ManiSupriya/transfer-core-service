package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.fundtransfer.dto.NpssEnrolmentRepoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpssEnrolmentRepository extends JpaRepository<NpssEnrolmentRepoDTO, Long>  {
    @Query(value = "SELECT b from NpssEnrolmentRepoDTO b where cif_id=:cif")
    Optional<NpssEnrolmentRepoDTO> getEnrolmentStatus(String cif);
    @Query(value = "SELECT TOP 200 * FROM [npss_enrollment] b " +
            "where is_default_account_updated is null or is_default_account_updated = :isDefaultAccountUpdated", nativeQuery=true)
    List<NpssEnrolmentRepoDTO> findAllByIsDefaultAccountUpdated(Boolean isDefaultAccountUpdated);
}
