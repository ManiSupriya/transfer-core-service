package com.mashreq.transfercoreservice.fundtransfer.repository;

import com.mashreq.transfercoreservice.model.TransferDetails;
import com.mashreq.transfercoreservice.model.TransferLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface TransferLimitRepository extends JpaRepository<TransferLimit, Long> {
    @Query(nativeQuery = true)
    TransferDetails findTransactionCountAndTotalAmountBetweenDates(Long beneficiaryId, Instant fromDate, Instant toDate);
}