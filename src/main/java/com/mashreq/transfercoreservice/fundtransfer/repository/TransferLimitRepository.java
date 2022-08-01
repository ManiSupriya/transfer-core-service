package com.mashreq.transfercoreservice.fundtransfer.repository;

import com.mashreq.transfercoreservice.model.TransferLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface TransferLimitRepository extends JpaRepository<TransferLimit, Long> {
    @Query("SELECT count(id) as id, SUM(paid_amount) as amount from TransactionLimit limit where limit" +
            ".beneficiaryId=:beneficiaryId and" +
            " limit.createdDate between :fromDate and :toDate")
    TransferLimit findTransactionCountAndTotalAmountBetweenDates(Long beneficiaryId, Instant fromDate,
                                                                 Instant toDate);
}