package com.mashreq.transfercoreservice.repository;

import com.mashreq.transfercoreservice.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByBeneficiaryTypeCode(String beneficiaryTypeCode);
    boolean existsPaymentHistoryByFinancialTransactionNo(String financialTransactionNo);

}
